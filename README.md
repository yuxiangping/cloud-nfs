# cloud-cache简介

CloudCache是一个简单的分布式文件缓存负载工具，用于在局域网内多台机器上进行文件存储。根据分配的节点大小进行负载，一台机器上可以分配多个节点。当节点分配的硬盘空间不足时发起警报，并从集群中剔除。

CloudCache仅仅是一个负载工具，为每次的存储请求分配一个可用的存储节点，对于文件的存储操作需要自己实现。

---

# cloud-cahce的使用

在config/cloud.xml中配置分配的存储节点

```xml
<nodes>
	<node>
		<name>CLOUD-1</name>
		<host>192.168.1.100</host>
		<port>22</port>
		<path>/data/cloud</path>
		<size unit="GB">10</size>
	</node>
	<node>
		<name>CLOUD-2</name>
		<host>192.168.1.101</host>
		<port>22</port>
		<path>/data/cloud</path>
		<size unit="TB">2</size>
	</node>
	<node>
		<name>CLOUD-3</name>
		<host>192.168.1.100</host>
		<port>22</port>
		<path>/data/cloud2</path>
		<size>800</size>
	</node>   
</nodes>
```

获取可用节点代码
```java
Node node = CloudManager.getNode();
```
