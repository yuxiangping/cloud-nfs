package com.cloud;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @info : 节点监控
 * @author: xiangping_yu
 * @data : 2013-12-2
 * @since : 1.5
 */
public class NodeMonitor implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(NodeMonitor.class);

	private StatisticHD statistic;
	
	@Override
	public void run() {
		List<Node> nodes = CacheCloud.getInstance().getNodeList();
		List<Node> copy = new ArrayList<Node>(nodes.size());
		for (Node node : nodes) {
			try {
				copy.add(node.clone());
			} catch (CloneNotSupportedException e) {
				logger.error("clone node '"+node.getName()+"' exception.", e);
			}
		}

		List<Node> warnNode = new ArrayList<Node>(nodes.size());
		for (Node node : copy) {
			double used = getUsed(node);
			node.setUsed(used);
			if (node.isWarn()) {
				logger.warn("cloud node warn, no available HD");
				warnNode.add(node);
			}
		}

		CacheCloud.getInstance().setNodeList(copy);
		CacheCloud.getInstance().setWarnList(warnNode);
	}

	/**
	 * 检测节点使用大小 (单们：MB)
	 */
	private double getUsed(Node node) {
		String host = node.getHost();
		String port = node.getPort();
		String path = node.getPath();
		logger.debug("monitor node used hard desk, node info ["+host+":"+port+path+"]");

		// FIXME 需要自己实现统计已用空间大小，实际应用中  我们使用nfs+portmap共享远程磁盘的方式来实现
		return statistic.usedHD(host, port, path);
	}
	
	public interface StatisticHD {
	  double usedHD(String host, String port, String path);
	}
}
