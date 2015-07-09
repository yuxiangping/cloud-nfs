package com.cloud;

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

import com.cloud.Node.ConfigRate;
import com.utils.closure.ClosureUtils;

/**
 * @info : 集群缓存
 * @author: xiangping_yu
 * @data : 2013-11-28
 * @since : 1.5
 */
public class CacheCloud {

  private static Logger logger = LoggerFactory.getLogger(CacheCloud.class);

  private static final long TIME_SECONDS = 2 * 60;
  
  private static final Random random = new Random();

  /**
   * 单例
   */
  private static CacheCloud instance = new CacheCloud();

  /**
   * 集群节点
   */
  private List<Node> nodes = new ArrayList<Node>();

  /**
   * 预警集群节点
   */
  private List<Node> warns = new ArrayList<Node>();

  private CacheCloud() {
    init();
    monitor();
  }

  public static CacheCloud getInstance() {
    return instance;
  }

  public Node getCloudNodeInfo() {
    Node node = getNodeByRate();
    return node;
  }

  /**
   * 刷新节点 (新增集群节点时内部调用执行刷新)
   */
  public boolean refresh() {
    return init();
  }

  /**
   * 初始化
   */
  @SuppressWarnings("unchecked")
  private boolean init() {
    try {
      logger.debug("init cloud file system cache node start...");
      InputStream is = CacheCloud.class.getResourceAsStream("/config/cloud.xml");
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
   */
  private void monitor() {
    NodeMonitor nodeMonitor = new NodeMonitor();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(nodeMonitor, 0, TIME_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 根据权重分配一个节点
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
      logger.error("Clusters cache node hard disk space is full.");
      throw new RuntimeException("Clusters cache node hard disk space is full.");
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
   */
  public synchronized List<Node> getNodeList() {
    return nodes;
  }

  /**
   * 重置集群节点信息
   */
  protected synchronized void setNodeList(List<Node> nodes) {
    this.nodes = nodes;
  }

  /**
   * 集群节点
   */
  public synchronized List<Node> getWarnList() {
    return warns;
  }

  /**
   * 重置预警集群节点信息
   */
  protected synchronized void setWarnList(List<Node> warns) {
    this.warns = warns;
  }

  /**
   * 统计节点硬盘大小
   */
  public double statisticalHD() {
    return ClosureUtils.statistic(nodes, Node.COUNT_SIZE, 0d);
  }

  /**
   * 统计已使用节点大小
   */
  public double usedHD() {
    return ClosureUtils.statistic(nodes, Node.COUNT_USED, 0d);
  }
}
