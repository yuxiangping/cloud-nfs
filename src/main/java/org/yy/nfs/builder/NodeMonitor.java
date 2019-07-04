package org.yy.nfs.builder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yy.nfs.CloudNFS;

/**
 * 节点监控
 * @author yy
 */
public class NodeMonitor implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(NodeMonitor.class);

  private CloudNFS cloud;
  private StatisticHD statistic;

  public NodeMonitor(CloudNFS cloud, StatisticHD statistic) {
    this.cloud = cloud;
    this.statistic = statistic;
  }
  
  @Override
  public void run() {
    List<Node> nodes = cloud.getNodeList();
    List<Node> copy = new ArrayList<Node>(nodes.size());
    for (Node node : nodes) {
      try {
        copy.add(node.clone());
      } catch (CloneNotSupportedException e) {
        logger.error("clone node '" + node.getName() + "' exception.", e);
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

    cloud.setNodeList(copy);
    cloud.setWarnList(warnNode);
  }

  /**
   * 检测节点使用大小 (单位：MB)
   */
  private double getUsed(Node node) {
    String host = node.getHost();
    String port = node.getPort();
    String path = node.getPath();
    logger.debug("monitor node used hard desk, node info [" + host + ":" + port + path + "]");
    return statistic.usedHD(host, port, path);
  }

  public interface StatisticHD {
    double usedHD(String host, String port, String path);
  }
}
