# Ansible Playbook for Optimizing Nginx RPS on Ubuntu

Below is an Ansible playbook that configures an Ubuntu server for maximum requests per second (RPS) with Nginx. The playbook includes:
- System tuning
- Nginx configuration testing with different options
- Performance testing with `wrk`
- Iterative optimization

```yaml
---
- name: Optimize Ubuntu server for maximum Nginx RPS
  hosts: all
  become: yes
  vars:
    # Test parameters
    wrk_threads: 4
    wrk_connections: 100
    wrk_duration: "30s"
    test_url: "http://localhost/"
    
    # Nginx tuning options to test
    nginx_worker_processes_options: ["auto", "2", "4", "8"]
    nginx_worker_connections_options: ["1024", "2048", "4096", "8192"]
    nginx_keepalive_options: ["0", "10", "30", "60"]
    nginx_multi_accept_options: ["off", "on"]
    
    # System tuning options
    sysctl_options:
      - { name: "net.ipv4.tcp_tw_reuse", value: "1" }
      - { name: "net.ipv4.tcp_fin_timeout", value: "30" }
      - { name: "net.ipv4.tcp_max_syn_backlog", value: "4096" }
      - { name: "net.core.somaxconn", value: "4096" }
      - { name: "net.ipv4.tcp_no_metrics_save", value: "1" }
      - { name: "net.core.rmem_max", value: "16777216" }
      - { name: "net.core.wmem_max", value: "16777216" }
      - { name: "net.ipv4.tcp_rmem", value: "4096 87380 16777216" }
      - { name: "net.ipv4.tcp_wmem", value: "4096 65536 16777216" }
    
    # Best configuration will be stored here
    best_config: {}
    best_rps: 0

  tasks:
    - name: Install required packages
      apt:
        name: "{{ item }}"
        state: present
        update_cache: yes
      loop:
        - nginx
        - build-essential
        - git
        - htop
        - sysstat
        - tuned
        - tuned-utils

    - name: Install wrk (HTTP benchmarking tool)
      git:
        repo: https://github.com/wg/wrk.git
        dest: /tmp/wrk
        version: master
      register: wrk_clone
      
    - name: Build wrk
      make:
        chdir: /tmp/wrk
        target: install
      when: wrk_clone.changed

    - name: Create directory for test results
      file:
        path: /var/log/nginx_optimization
        state: directory
        mode: '0755'

    - name: Apply system tuning parameters
      sysctl:
        name: "{{ item.name }}"
        value: "{{ item.value }}"
        state: present
        reload: yes
      loop: "{{ sysctl_options }}"

    - name: Configure tuned profile for network throughput
      command: tuned-adm profile throughput-performance
      changed_when: false

    - name: Increase file descriptor limits
      pam_limits:
        domain: '*'
        limit_type: 'soft'
        limit_item: nofile
        value: '65536'
      - name: Increase file descriptor limits (hard)
      pam_limits:
        domain: '*'
        limit_type: 'hard'
        limit_item: nofile
        value: '65536'

    - name: Test different Nginx configurations
      include_tasks: nginx_config_test.yml
      loop: "{{ nginx_worker_processes_options }}"
      loop_control:
        loop_var: worker_processes

    - name: Apply best Nginx configuration
      template:
        src: nginx.conf.j2
        dest: /etc/nginx/nginx.conf
        owner: root
        group: root
        mode: '0644'
      vars:
        worker_processes: "{{ best_config.worker_processes }}"
        worker_connections: "{{ best_config.worker_connections }}"
        keepalive_timeout: "{{ best_config.keepalive_timeout }}"
        multi_accept: "{{ best_config.multi_accept }}"

    - name: Restart Nginx to apply final configuration
      service:
        name: nginx
        state: restarted

    - name: Display optimization results
      debug:
        msg: |
          Best configuration found:
          - Worker processes: {{ best_config.worker_processes }}
          - Worker connections: {{ best_config.worker_connections }}
          - Keepalive timeout: {{ best_config.keepalive_timeout }}
          - Multi accept: {{ best_config.multi_accept }}
          - Achieved RPS: {{ best_rps | round(0) }}
```

## nginx_config_test.yml (included file)

```yaml
- name: Test worker_connections options
  include_tasks: test_worker_connections.yml
  loop: "{{ nginx_worker_connections_options }}"
  loop_control:
    loop_var: worker_connections
```

## test_worker_connections.yml (included file)

```yaml
- name: Test keepalive_timeout options
  include_tasks: test_keepalive_timeout.yml
  loop: "{{ nginx_keepalive_options }}"
  loop_control:
    loop_var: keepalive_timeout
```

## test_keepalive_timeout.yml (included file)

```yaml
- name: Test multi_accept options
  include_tasks: test_multi_accept.yml
  loop: "{{ nginx_multi_accept_options }}"
  loop_control:
    loop_var: multi_accept
```

## test_multi_accept.yml (included file)

```yaml
- name: Create temporary Nginx config
  template:
    src: nginx.conf.j2
    dest: /etc/nginx/nginx.conf.tmp
    owner: root
    group: root
    mode: '0644'
  vars:
    worker_processes: "{{ worker_processes }}"
    worker_connections: "{{ worker_connections }}"
    keepalive_timeout: "{{ keepalive_timeout }}"
    multi_accept: "{{ multi_accept }}"

- name: Apply temporary Nginx config
  command: cp /etc/nginx/nginx.conf.tmp /etc/nginx/nginx.conf

- name: Restart Nginx
  service:
    name: nginx
    state: restarted

- name: Warm up Nginx
  command: wrk -t2 -c100 -d10s {{ test_url }}
  ignore_errors: yes
  changed_when: false

- name: Run performance test with wrk
  command: wrk -t{{ wrk_threads }} -c{{ wrk_connections }} -d{{ wrk_duration }} {{ test_url }}
  register: wrk_output
  changed_when: false

- name: Extract RPS from wrk output
  set_fact:
    current_rps: "{{ (wrk_output.stdout | regex_search('Requests/sec:\\s+(\\d+\\.?\\d*)'))[1] | float }}"

- name: Record test results
  lineinfile:
    path: /var/log/nginx_optimization/results.log
    line: "worker_processes={{ worker_processes }}, worker_connections={{ worker_connections }}, keepalive={{ keepalive_timeout }}, multi_accept={{ multi_accept }} - RPS: {{ current_rps | round(0) }}"
    create: yes

- name: Check if this is the best configuration so far
  set_fact:
    best_config: "{ 'worker_processes': '{{ worker_processes }}', 'worker_connections': '{{ worker_connections }}', 'keepalive_timeout': '{{ keepalive_timeout }}', 'multi_accept': '{{ multi_accept }}' }"
    best_rps: "{{ current_rps }}"
  when: current_rps > best_rps
```

## nginx.conf.j2 (template file)

```nginx
user www-data;
worker_processes {{ worker_processes }};
pid /run/nginx.pid;
include /etc/nginx/modules-enabled/*.conf;

events {
    worker_connections {{ worker_connections }};
    {% if multi_accept == "on" %}multi_accept on;{% else %}multi_accept off;{% endif %}
    use epoll;
}

http {
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout {{ keepalive_timeout }};
    types_hash_max_size 2048;
    
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    gzip on;
    
    include /etc/nginx/conf.d/*.conf;
    include /etc/nginx/sites-enabled/*;
}
```

## How to Use This Playbook

1. Save the main playbook as `optimize_nginx.yml`
2. Save the included files in the same directory
3. Save the template as `templates/nginx.conf.j2`
4. Create an inventory file with your server(s)
5. Run with: `ansible-playbook -i inventory optimize_nginx.yml`

## Notes

- The playbook will test all combinations of the specified Nginx parameters
- After each configuration change, it runs `wrk` to measure RPS
- It keeps track of the best-performing configuration
- Finally applies the best configuration and displays the results
- You may want to adjust the test parameters (threads, connections, duration) based on your server capacity
- Consider running this during low-traffic periods as it will restart Nginx multiple times

Would you like me to explain any specific part in more detail or make adjustments to the approach?