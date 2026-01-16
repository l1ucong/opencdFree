# opencdFree 部署说明

第一次分享个人项目，希望大家使用顺利，并提出改进意见~

---

## 🛠 环境要求

* **构建工具**: Maven 3.9.11 或更高版本
* **运行环境**: Java 8 (JRE/JDK)
* **操作系统**: 推荐 Linux 

---

## 🚀 部署流程

### 1. 项目打包
在本地开发环境中使用 IntelliJ IDEA 的 **Maven** 面板执行 `package` 命令，或在项目根目录运行：
```bash
mvn clean package -DskipTests
```
成功后，在 target 目录下获取 opencdFree.jar。

### 2. 环境配置
根据qb的实际情况，修改  `application.yml` 中的配置信息（如数据库连接、端口号、密钥等）。

### 3. 上传资源
将以下文件上传至服务器的目标部署路径（例如：/root/opencd）：

**opencdFree.jar —— 核心运行程序**

**application.yml —— 外部配置文件**

**run.sh —— 服务管理脚本**

### 4. 脚本配置与授权
4.1 修改路径：编辑 `run.sh`，确保 `APP_HOME` 变量指向第 3 步中的实际上传路径。
4.2 授予权限：执行以下命令使脚本具备可执行权限：
```bash
chmod +x run.sh
```

### 5.  服务管理
项目启动后会默认加载同级目录下的 application.yml 配置文件。

| 操作命令               | 功能描述 |
|:-------------------| :--- |
| `./run.sh start`   | 启动后台服务 |
| `./run.sh stop`    | 停止当前服务 |
| `./run.sh restart` | 重启服务 |
| `tail -f app/logs` | 查看服务运行状态 |

