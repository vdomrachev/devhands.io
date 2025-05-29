# inventory.ini
[ubuntu_servers]
server1 ansible_host=192.168.1.100 ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_rsa
server2 ansible_host=192.168.1.101 ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_rsa

[ubuntu_servers:vars]
ansible_become=yes
ansible_become_method=sudo

---

# Setup Instructions

## Prerequisites
1. Install Ansible on your local machine:
   ```bash
   sudo apt update
   sudo apt install ansible
   # or
   pip install ansible
   ```

2. Ensure SSH access to target servers:
   ```bash
   ssh-copy-id ubuntu@your-server-ip
   ```

## Directory Structure
Create the following directory structure:
```
nginx-performance/
├── playbook.yml
├── inventory.ini
└── templates/
    ├── nginx_baseline.conf.j2
    ├── nginx_optimized.conf.j2
    └── nginx_gzip.conf.j2
```

## Usage

1. Update inventory.ini with your server details
2. Run the playbook:
   ```bash
   cd nginx-performance
   ansible-playbook -i inventory.ini playbook.yml
   ```

3. For verbose output:
   ```bash
   ansible-playbook -i inventory.ini playbook.yml -v
   ```

4. To run on specific hosts:
   ```bash
   ansible-playbook -i inventory.ini playbook.yml --limit server1
   ```

## Custom Variables
You can override default variables:
```bash
ansible-playbook -i inventory.ini playbook.yml \
  -e "test_connections=500" \
  -e "test_duration=60s" \
  -e "nginx_worker_connections=16384"
```

## Results
- Results are saved on target servers in `/tmp/nginx_performance_results/`
- Results are automatically fetched to local `./nginx_results/[hostname]/`
- Summary file contains RPS comparison across all tests

## What the Playbook Tests
1. **Baseline Configuration**: Standard nginx setup
2. **Optimized Configuration**: High-performance tuning
3. **Connection Scaling**: Tests with 50, 100, 200, 500, 1000 connections
4. **Gzip Compression**: Performance impact of compression
5. **System Tuning**: Kernel parameters for high connection counts

## Performance Optimizations Applied
- **System Level**:
    - Increased file descriptor limits
    - TCP stack tuning
    - Network buffer optimization
    - Port range expansion

- **Nginx Level**:
    - Worker process optimization
    - Connection pooling
    - Buffer tuning
    - Keep-alive optimization
    - Static file caching
    - Open file cache

## Monitoring During Tests
The playbook installs monitoring tools (htop, iotop, sysstat) you can use:
```bash
# On target server during tests:
htop           # CPU and memory usage
iotop          # Disk I/O
ss -tuln       # Network connections