package de.braintags.io.vertx.util.security;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A CRUDPermissionMap manages permissions for different keys, where a key can be a role / group, a class-definition
 * etc.
 * 
 * @author Michael Remme
 * 
 */
public class CRUDPermissionMap {
  /**
   * The name of the key, which defines the default permission
   */
  public static final String DEFAULT_PERMISSION_KEY_NAME = "*";
  private Map<String, BitSet> permissionMap = new HashMap<>();

  /**
   * Creates a new instance, where the permissions for the default key are set
   */
  public CRUDPermissionMap() {
    permissionMap.put(DEFAULT_PERMISSION_KEY_NAME, new BitSet());
  }

  /**
   * Creates a new instance by parsing the input string, which must be in the format:
   * admin{CRUD};users{R}
   * 
   * @param perms
   */
  public CRUDPermissionMap(String perms) {
    if (perms == null) {
      throw new NullPointerException("permission string is null");
    }
    String[] ps = perms.split(";");
    for (String p : ps) {
      addPermissionEntry(p);
    }
  }

  /**
   * Adds one permission sequence like "admin{CRUD}", which is parsed and added into the current map
   * 
   * @param permission
   *          the permission sequence to be added
   *          return the name of the key, which was added, in the example that would be "admin"
   * @return the key, which was added
   */
  public String addPermissionEntry(String permission) {
    int index = permission.indexOf('{');
    if (index > 0) {
      String key = permission.substring(0, index).trim();
      String actions = permission.substring(index + 1).replaceAll("}", "").trim();
      addPermissions(key, actions);
      return key;
    } else {
      String key = permission.trim();
      permissionMap.put(key, new BitSet());
      return key;
    }
  }

  /**
   * Set the permission for the given action for the given key to true
   * 
   * @param key
   *          the key to be set, for example "admin" for a role
   * @param action
   *          the action to be allowed, one of CRUD
   */
  public void addPermission(String key, char action) {
    setPermission(key, action, true);
  }

  /**
   * Set the permissions for the given actions for the given key to true
   * 
   * @param key
   *          the key to be set, for example "admin" for a role
   * @param actions
   *          the actions to be allowed, could be "CRD" for example
   */
  public void addPermissions(String key, String actions) {
    char[] chars = actions.toCharArray();
    for (char c : chars) {
      setPermission(key, c, true);
    }
  }

  /**
   * Set the permission for the given action for the given key to false
   * 
   * @param key
   *          the key to be set, for example "admin" for a role
   * @param action
   *          the action to be disallowed, one of CRUD
   */
  public void removePermission(String key, char action) {
    setPermission(key, action, false);
  }

  /**
   * Checks the permission for the given key and action. If for the key no definition is found, the default definition
   * will be used.
   * 
   * @param key
   *          the key to be checked, for example "admin" for a role
   * @param action
   *          the action to be checked, one of CRUD
   * @return true, if permission is granted, false otherwise
   */
  public boolean hasPermission(String key, char action) {
    BitSet set = permissionMap.get(key);
    if (set == null) {
      set = permissionMap.get(DEFAULT_PERMISSION_KEY_NAME);
    }
    return hasPermission(set, action);
  }

  private void setPermission(String key, char action, boolean activate) {
    if (!permissionMap.containsKey(key)) {
      permissionMap.put(key, new BitSet());
    }
    setAction(permissionMap.get(key), action, activate);
  }

  private boolean hasPermission(BitSet set, char action) {
    switch (action) {
    case 'C':
      return set.get(0);
    case 'R':
      return set.get(1);
    case 'U':
      return set.get(2);
    case 'D':
      return set.get(3);
    default:
      throw new UnsupportedOperationException("action is not supported: '" + action + "'");
    }
  }

  private void setAction(BitSet set, char action, boolean activate) {
    switch (action) {
    case 'C':
      set.set(0, activate);
      break;
    case 'R':
      set.set(1, activate);
      break;
    case 'U':
      set.set(2, activate);
      break;
    case 'D':
      set.set(3, activate);
      break;
    default:
      throw new UnsupportedOperationException("action is not supported: " + action);
    }
  }

  /**
   * Get the names of all defined keys inside the permission map
   * 
   * @return
   */
  public Set<String> getKeys() {
    return permissionMap.keySet();
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    Set<Entry<String, BitSet>> eSet = permissionMap.entrySet();
    eSet.forEach(entry -> addEntry(buffer, entry));
    return buffer.toString();
  }

  private void addEntry(StringBuilder buffer, Entry<String, BitSet> entry) {
    if (buffer.length() != 0) {
      buffer.append("; ");
    }
    buffer.append(entry.getKey()).append("{");
    addPermission(buffer, entry.getValue(), 'C');
    addPermission(buffer, entry.getValue(), 'R');
    addPermission(buffer, entry.getValue(), 'U');
    addPermission(buffer, entry.getValue(), 'D');
    buffer.append("}");
  }

  private void addPermission(StringBuilder buffer, BitSet set, char action) {
    boolean hasPermission = hasPermission(set, action);
    buffer.append(hasPermission ? action : "");
  }
}
