package org.ros.internal.node.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.client.SlaveClient;
import org.ros.namespace.GraphName;

/**
 * A ROS parameter server.
 * 
 * @author Groff
 */
public class ParameterServer {

  private static final Log log = LogFactory.getLog(ParameterServer.class);

  private final Map<String, Object> tree;
  private final Map<GraphName, List<NodeIdentifier>> subscribers;
  private final GraphName masterName;

  public ParameterServer() {
    tree = new ConcurrentHashMap<String, Object>();
    subscribers = new ConcurrentHashMap<GraphName, List<NodeIdentifier>>();
    masterName = GraphName.of("/master");
  }

  public void subscribe(GraphName name, NodeIdentifier nodeIdentifier) {
	List<NodeIdentifier> subs = subscribers.get(name);
	if( subs == null ) {
		subs = new ArrayList<NodeIdentifier>();
		subscribers.put(name, subs);
	}
	subs.add(nodeIdentifier);
  }

  private Stack<String> getGraphNameParts(GraphName name) {
    Stack<String> parts = new Stack<String>();
    GraphName tip = name;
    while (!tip.isRoot()) {
      parts.add(tip.getBasename().toString());
      tip = tip.getParent();
    }
    return parts;
  }

  @SuppressWarnings("unchecked")
  public Object get(GraphName name) {
    assert(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Object possibleSubtree = tree;
    while (!parts.empty() && possibleSubtree != null) {
      if (!(possibleSubtree instanceof Map)) {
        return null;
      }
      possibleSubtree = ((Map<String, Object>) possibleSubtree).get(parts.pop());
    }
    return possibleSubtree;
  }

  @SuppressWarnings("unchecked")
  private void setValue(GraphName name, Object value) {
    assert(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Map<String, Object> subtree = tree;
    while (!parts.empty()) {
      String part = parts.pop();
      if (parts.empty()) {
        subtree.put(part, value);
      } else if (subtree.containsKey(part) && subtree.get(part) instanceof Map) {
        subtree = (Map<String, Object>) subtree.get(part);
      } else {
        Map<String, Object> newSubtree = new HashMap<String, Object>();
        subtree.put(part, newSubtree);
        subtree = newSubtree;
      }
    }
  }

  private interface Updater {
    void update(SlaveClient client);
  }

  private <T> void update(GraphName name, T value, Updater updater) {
    setValue(name, value);
    synchronized (subscribers) {
      for (NodeIdentifier nodeIdentifier : subscribers.get(name)) {
        SlaveClient client = new SlaveClient(masterName, nodeIdentifier.getUri());
        try {
          updater.update(client);
        } catch (Exception e) {
          log.error(e);
        }
      }
    }
  }

  public void set(final GraphName name, final boolean value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  public void set(final GraphName name, final int value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  public void set(final GraphName name, final double value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  public void set(final GraphName name, final String value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  public void set(final GraphName name, final List<?> value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  public void set(final GraphName name, final Map<?, ?> value) {
    update(name, value, new Updater() {
      @Override
      public void update(SlaveClient client) {
        client.paramUpdate(name, value);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public void delete(GraphName name) {
    assert(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Map<String, Object> subtree = tree;
    while (!parts.empty() && subtree.containsKey(parts.peek())) {
      String part = parts.pop();
      if (parts.empty()) {
        subtree.remove(part);
      } else {
        subtree = (Map<String, Object>) subtree.get(part);
      }
    }
  }

  public Object search(GraphName name) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public boolean has(GraphName name) {
    assert(name.isGlobal());
    Stack<String> parts = getGraphNameParts(name);
    Map<String, Object> subtree = tree;
    while (!parts.empty() && subtree.containsKey(parts.peek())) {
      String part = parts.pop();
      if (!parts.empty()) {
        subtree = (Map<String, Object>) subtree.get(part);
      }
    }
    return parts.empty();
  }

  @SuppressWarnings("unchecked")
  private Set<GraphName> getSubtreeNames(GraphName parent, Map<String, Object> subtree,
      Set<GraphName> names) {
    for (String name : subtree.keySet()) {
      Object possibleSubtree = subtree.get(name);
      if (possibleSubtree instanceof Map) {
        names.addAll(getSubtreeNames(parent.join(GraphName.of(name)),
            (Map<String, Object>) possibleSubtree, names));
      } else {
        names.add(parent.join(GraphName.of(name)));
      }
    }
    return names;
  }

  public Collection<GraphName> getNames() {
    Set<GraphName> names = new HashSet<GraphName>();
    return getSubtreeNames(GraphName.root(), tree, names);
  }

}
