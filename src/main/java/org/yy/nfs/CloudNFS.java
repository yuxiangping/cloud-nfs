package org.yy.nfs;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yy.nfs.builder.Node;
import org.yy.nfs.builder.Node.ConfigRate;
import org.yy.nfs.builder.NodeMonitor.StatisticHD;
import org.yy.nfs.builder.NodeBuilder;
import org.yy.nfs.builder.NodeMonitor;

/**
 * NFS集群
 * @author yy
 */
public class CloudNFS {

  private static Logger logger = LoggerFactory.getLogger(CloudNFS.class);

  private static final long TIME_SECONDS = 2 * 60;

  private static final Random random = new Random();

  /**
   * 集群节点
   */
  private List<Node> nodes = new ArrayList<Node>();

  /**
   * 预警集群节点
   */
  private List<Node> warns = new ArrayList<Node>();

  
  // 需要自己实现统计已用空间大小，实际应用中 我们使用nfs+portmap共享远程磁盘的方式来实现
  public CloudNFS(StatisticHD statistic) {
    init();
    monitor(statistic);
  }

  public Node getCloudNode() {
    Node node = getNodeByRate();
    return node;
  }

  /**
   * 刷新节点 (新增集群节点时内部调用执行刷新)
   * @return 新配置是否刷新成功
   */
  public boolean refresh() {
    return init();
  }

  @SuppressWarnings("unchecked")
  private boolean init() {
    try {
      logger.debug("init cloud file system cache node start...");
      InputStream is = CloudNFS.class.getResourceAsStream("/config/cloud.xml");
      SAXReader reader = new SAXReader();
      Document doc = reader.read(new InputStreamReader(is, "UTF-8"));
      Element root = doc.getRootElement();

      List<Node> list = new ArrayList<Node>();
      for (Element element : (List<Element>) root.elements()) {
        list.add(NodeBuilder.bulider(element));
      }

      nodes = list;
    } catch (Exception e) {
      logger.error("Load cloud.xml error", e);
      return false;
    }
    logger.debug("init cloud file system cache node finished.");
    return true;
  }

  /**
   * 对集群节点进行监控
   * @param statistic 节点用量监控程式
   */
  private void monitor(StatisticHD statistic) {
    NodeMonitor nodeMonitor = new NodeMonitor(this, statistic);
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(nodeMonitor, 0, TIME_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 根据权重自动分配一个节点
   * @return 返回可用节点
   */
  private Node getNodeByRate() {
    List<ConfigRate> rates = new ArrayList<ConfigRate>(nodes.size());

    int current = 0;
    for (Node node : nodes) {
      // 已预警节点不再分配
      if (!node.isWarn()) {
        current += node.getAvailable();
        rates.add(new ConfigRate(node, current));
      }
    }

    if (current <= 0) {
      throw new RuntimeException("no available cache node.");
    }

    int rand = random.nextInt(current);
    for (ConfigRate rate : rates) {
      if (rate.isHit(rand)) {
        return rate.getNode();
      }
    }
    return null;
  }

  /**
   * 集群节点
   * @return 返回全部节点信息
   */
  public List<Node> getNodeList() {
    return nodes;
  }

  /**
   * 重置集群节点信息
   * @param nodes 新节点列表
   */
  public void setNodeList(List<Node> nodes) {
    this.nodes = nodes;
  }

  /**
   * 已告警集群节点
   * @return 返回告警节点列表 
   */
  public List<Node> getWarnList() {
    return warns;
  }

  /**
   * 重置预警集群节点信息
   * @param warns 最新的告警节点列表
   */
  public void setWarnList(List<Node> warns) {
    this.warns = warns;
  }

  /**
   * 统计节点硬盘大小
   * @return 返回总容量大小
   */
  public double statisticalHD() {
    return nodes.stream().mapToDouble(Node::getSize).sum();
  }

  /**
   * 统计已使用节点大小
   * @return 返回总使用大小
   */
  public double usedHD() {
    return nodes.stream().mapToDouble(Node::getUsed).sum();
  }

}
