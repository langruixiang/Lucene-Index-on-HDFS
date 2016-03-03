##Put Lucene Index on HDFS
###项目介绍
Lucene是一个开源的全文检索引擎工具包，Lucene介绍请参见[Lucene维基百科](https://en.wikipedia.org/wiki/Lucene)。原生Lucene对检索文件分词、建立倒排索引、查询倒排索引，排序返回结果。当大量用户访问搜索引擎时，单机无法处理承受大量用户访问，需要多台服务器共同提供服务，但是原生的Lucene并没有提供倒排索引共享的功能，意味着不同服务器需要分别建立倒排索引提供搜索服务。</br>

本项目想法是拆解Lucene各功能模块:</br>

* 将分词，建立倒排索引功能放在索引服务器，索引服务器建立索引后，将索引上传至HDFS。</br>
* 将查询倒排索引，排序返回结果放在搜索服务器，搜索服务器通过HDFS获得索引服务器建立的倒排索引</br>
* 负载均衡服务器负责转发用户请求至不同搜索服务器</br>

项目最终呈现是一个简单配置即可使用的工具，配置项目的xml配置文件，并将同一份xml文件分别拷贝至不同服务器相应路径，即可使用。xml文件具体配置请参见下文

###系统结构
系统结构如图所示：

![](https://github.com/langruixiang/Lucene-Index-on-HDFS/blob/master/systemstructure.png)

* 用户：用户的请求首先连接负载均衡服务器，负载均衡服务起返回用户搜索服务器的ip地址，进而用户向搜索服务器发起搜索请求。负载均衡过程对用户透明。</br>
* 搜索服务器：搜索服务器在接收到索引服务器索引更新的消息后，检查搜索服务器MD5，并将有差异的索引文件更新至本机。</br>
* 索引服务器：索引服务器定期更新索引，每次索引更新完毕上传索引至HDFS，并向所有的搜索服务器发送索引更新消息。
* 负载均衡服务起：接收用户请求，向用户返回恰当搜索服务器IP。

###开发环境
本项目采用Eclipse开发，pull本项目导入Eclipse即可开发，更改配置文件即可运行。
###源码包介绍
src/目录下为源码</br>
src/BalanceServer为负载均衡服务器源码</br>

* BalanceServer.java 负责读取配置文件初始化负载均衡服务器</br>
* BalanceServerMain.java 负责启动负载均衡服务器</br>
* BalanceServerThread.java 负责建立网络连接处理网络通信</br>

src/Client为客户端源码

* ClientBalanceThread.java 负责客户端与负载均衡服务器的通信
* ClientMain.java 为客户端代码入口
* ClientSearch.java 负责客户端与搜索服务器的通信
* ClientThread.java 负责模仿多用户并发搜索并统计延时和网络错误

src/DiskFileOperation为操作本机硬盘文件的函数源码

* DiskFileOperation.java 清空系统临时文件

src/IndexServer为索引服务器源码

* CreateIndex.java 负责建立倒排索引
* IndexServer.java 负责读取配置文件初始化服务器并将倒排索引上传至HDFS
* IndexServerMain.java 索引服务器入口
* IndexServerThread.java 负责索引服务器与搜索服务器的网络通讯

src/Net 为通讯协议

* NetCmd.java 为通信消息的定义及编解码 
* NetUtility.java 为网络辅助函数

src/SearchServer为搜索服务器源码

* SearchIndex.java 负责搜索倒排索引获得搜索结果
* SearchServer.java 负责读取配置文件初始化搜索服务器并从HDFS获得最新索引
* SearchServerMain.java 索引服务器入口
* SearchThread.java 负责与客户端的通信
* UpdateIndexThread.java 负责与索引服务器通信
###配置文件
根目录下config.xml为配置文件
默认配置如下：</br>

	<configuration>
	
	    <BalanceServer>
	        <IP>127.0.0.1</IP>
	    </BalanceServer>
	
	    <IndexServer>
	        <serverName>indexServer1</serverName>
	        <IP>127.0.0.1</IP>
	        <dataPath>/home/lang/workspace/BigWeb/WeiBo.xml</dataPath>
	        <indexTmpPath>/home/lang/workspace/BigWeb/index/</indexTmpPath>
	        <hdfsPath>hdfs://localhost:9000/user/lucene/index/</hdfsPath>
	    </IndexServer>
	
	    <SearchServer>
	        <serverName>searcherServer1</serverName>
	        <IP>124.16.103.3</IP>
	        <indexTmpPath>/home/administrator/workspace/BigWeb/index/</indexTmpPath>
	        <hdfsPath>hdfs://localhost:9000/user/lucene/index/</hdfsPath>
	    </SearchServer>
	
	    <SearchServer>
	        <serverName>searcherServer2</serverName>
	        <IP>127.0.0.1</IP>
	        <indexTmpPath>/home/lang/workspace/BigWeb/index/</indexTmpPath>
	        <hdfsPath>hdfs://localhost:9000/user/lucene/index/</hdfsPath>
	    </SearchServer>
	</configuration>

* BalanceServer节点配置负载均衡服务器IP地址
* IndexServer配置索引服务器</br>
  serverName为索引服务器标识符</br>
  IP为索引服务器IP地址</br>
  dataPath为待检索文件根目录</br>
  indexTmpPath为系统临时文件目录</br>
  hdfsPath为HDFS存放倒排索引目录</br>
* SearchServer配置搜索服务器
  serverName为搜索服务器标识符，不同搜索服务器标识符应不同
  IP当前搜索服务器IP地址
  indexTmpPath系统临时文件目录
  hdfsPath为HDFS倒排索引目录
  其他SearchServer配置与之相同
正确配置配置文件后，因为本系统目前没有用户节点，更改ClientThread.java即可搜索结果或测试系统延时和错误率
