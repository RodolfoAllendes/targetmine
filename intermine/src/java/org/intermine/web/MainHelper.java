package org.intermine.web;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Date;
import java.math.BigDecimal;

import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.AttributeDescriptor;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.metadata.Model;
import org.intermine.metadata.ReferenceDescriptor;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryEvaluable;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryNode;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ClassConstraint;
import org.intermine.util.TypeUtil;
import org.intermine.util.StringUtil;

/**
 * Helper methods for main controller and main action
 * @author Mark Woodbridge
 */
public class MainHelper
{
    /**
     * Given a path, render a set of metadata Nodes to the relevant depth
     * @param path of form Gene.organism.name
     * @param model the model used to resolve class names
     * @return an ordered Set of nodes
     */
    public static Collection makeNodes(String path, Model model) {
        String className, subPath;
        if (path.indexOf(".") == -1) {
            className = path;
            subPath = "";
        } else {
            className = path.substring(0, path.indexOf("."));
            subPath = path.substring(path.indexOf(".") + 1);
        }
        Map nodes = new LinkedHashMap();
        nodes.put(className, new MetadataNode(className));
        makeNodes(getClassDescriptor(className, model), subPath, className, nodes);
        return nodes.values();
    }

    /**
     * Recursive method used to add nodes to a set representing a path from a given ClassDescriptor
     * @param cld the root ClassDescriptor
     * @param path current path prefix (eg Gene)
     * @param currentPath current path suffix (eg organism.name)
     * @param nodes the current Node set
     */
    protected static void makeNodes(ClassDescriptor cld, String path, String currentPath,
                                    Map nodes) {
        List sortedNodes = new ArrayList();

        // compare FieldDescriptors by name
        Comparator comparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                String fieldName1 = ((FieldDescriptor) o1).getName().toLowerCase();
                String fieldName2 = ((FieldDescriptor) o2).getName().toLowerCase();
                return fieldName1.compareTo(fieldName2);
            }
        };

        Set attributeNodes = new TreeSet(comparator);
        Set referenceAndCollectionNodes = new TreeSet(comparator);
        for (Iterator i = cld.getAllFieldDescriptors().iterator(); i.hasNext();) {
            FieldDescriptor fd = (FieldDescriptor) i.next();
            if (!fd.isReference() && !fd.isCollection()) {
                attributeNodes.add(fd);
            } else {
                referenceAndCollectionNodes.add(fd);
            }
        }

        sortedNodes.addAll(attributeNodes);
        sortedNodes.addAll(referenceAndCollectionNodes);

        for (Iterator i = sortedNodes.iterator(); i.hasNext();) {
            FieldDescriptor fd = (FieldDescriptor) i.next();
            String fieldName = fd.getName();

            if (fieldName.equals("id")) {
                continue;
            }

            String head, tail;
            if (path.indexOf(".") != -1) {
                head = path.substring(0, path.indexOf("."));
                tail = path.substring(path.indexOf(".") + 1);
            } else {
                head = path;
                tail = "";
            }

            String button;
            if (fieldName.equals(head)) {
                button = "-";
            } else if (fd.isReference() || fd.isCollection()) {
                button = "+";
            } else {
                button = " ";
            }

            MetadataNode parent = (MetadataNode) nodes.get(currentPath);
            MetadataNode node = new MetadataNode(parent, fieldName, button);
            node.setModel(cld.getModel());

            nodes.put(node.getPath(), node);
            if (fieldName.equals(head)) {
                ClassDescriptor refCld = ((ReferenceDescriptor) fd).getReferencedClassDescriptor();
                makeNodes(refCld, tail, currentPath + "." + head, nodes);
            }
        }
    }

    /**
     * Make an InterMine query from a path query
     * @param query the PathQuery
     * @param savedBags the current saved bags map
     * @return an InterMine Query
     */
    public static Query makeQuery(PathQuery query, Map savedBags) {
        return makeQuery(query, savedBags, null);
    }

    /**
     * Make an InterMine query from a path query
     * @param query the PathQuery
     * @param savedBags the current saved bags map
     * @param pathToQueryNode optional parameter in which path to QueryNode map can be returned
     * @return an InterMine Query
     */
    public static Query makeQuery(PathQuery query, Map savedBags, Map pathToQueryNode) {
        query = (PathQuery) query.clone();
        Map qNodes = query.getNodes();
        List view = query.getView();
        Model model = query.getModel();

        //first merge the query and the view
        for (Iterator i = view.iterator(); i.hasNext();) {
            String path = (String) i.next();
            if (!qNodes.containsKey(path)) {
                query.addNode(path);
            }
        }

        //create the real query
        Query q = new Query();
        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
        q.setConstraint(cs);

        Map queryBits = new HashMap();

        //build the FROM and WHERE clauses
        for (Iterator i = query.getNodes().values().iterator(); i.hasNext();) {
            PathNode node = (PathNode) i.next();
            String path = node.getPath();
            QueryReference qr = null;

            if (path.indexOf(".") == -1) {
                QueryClass qc = new QueryClass(getClass(node.getType(), model));
                q.addFrom(qc);
                queryBits.put(path, qc);
            } else {
                String fieldName = node.getFieldName();
                QueryClass parentQc = (QueryClass) queryBits.get(node.getPrefix());

                if (node.isAttribute()) {
                    QueryField qf = new QueryField(parentQc, fieldName);
                    queryBits.put(path, qf);
                } else {
                    if (node.isReference()) {
                        qr = new QueryObjectReference(parentQc, fieldName);
                    } else {
                        qr = new QueryCollectionReference(parentQc, fieldName);
                    }
                    QueryClass qc = new QueryClass(getClass(node.getType(), model));
                    cs.addConstraint(new ContainsConstraint(qr, ConstraintOp.CONTAINS, qc));
                    q.addFrom(qc);
                    queryBits.put(path, qc);
                }
            }

            QueryNode qn = (QueryNode) queryBits.get(path);
            for (Iterator j = node.getConstraints().iterator(); j.hasNext();) {
                Constraint c = (Constraint) j.next();
                if (BagConstraint.VALID_OPS.contains(c.getOp())) {
                    cs.addConstraint(new BagConstraint(qn,
                                                       c.getOp(),
                                                       (Collection) savedBags.get(c.getValue())));
                } else if (node.isAttribute()) { //assume, for now, that it's a SimpleConstraint
                    if (c.getOp() == ConstraintOp.IS_NOT_NULL
                        || c.getOp() == ConstraintOp.IS_NULL) {
                        cs.addConstraint(new SimpleConstraint((QueryEvaluable) qn,
                                                              c.getOp()));
                    } else {
                        cs.addConstraint(new SimpleConstraint((QueryField) qn,
                                                              c.getOp(),
                                                              new QueryValue(c.getValue())));
                    }
                } else if (node.isReference()) {
                    if (c.getOp() == ConstraintOp.IS_NOT_NULL
                        || c.getOp() == ConstraintOp.IS_NULL) {
                        cs.addConstraint(
                            new ContainsConstraint((QueryObjectReference) qr, c.getOp()));
                    }
                }
            }
        }
        
        // Now process loop constraints. The constraint parameter refers backwards and
        // forwards in the query so we can't process these in the above loop.
        for (Iterator i = query.getNodes().values().iterator(); i.hasNext();) {
            PathNode node = (PathNode) i.next();
            String path = node.getPath();
            QueryNode qn = (QueryNode) queryBits.get(path);
            
            for (Iterator j = node.getConstraints().iterator(); j.hasNext();) {
                Constraint c = (Constraint) j.next();
                if (node.isReference() && c.getOp() != ConstraintOp.IS_NOT_NULL
                    && c.getOp() != ConstraintOp.IS_NULL) {
                    QueryClass refQc = (QueryClass) queryBits.get(c.getValue());
                        cs.addConstraint(new ClassConstraint((QueryClass) qn, c.getOp(), refQc));
                }
            }
        }

        //build the SELECT list
        for (Iterator i = view.iterator(); i.hasNext();) {
            q.addToSelect((QueryNode) queryBits.get((String) i.next()));
        }

        //caller might want path to query node map (e.g. PrecomputeTask)
        if (pathToQueryNode != null) {
            pathToQueryNode.putAll(queryBits);
        }

        return q;
    }

    /**
     * Instantiate a class by unqualified name
     * The name should be "InterMineObject" or the name of class in the model provided
     * @param className the name of the class
     * @param model the Model used to resolve class names
     * @return the relevant Class
     */
    public static Class getClass(String className, Model model) {
        if ("InterMineObject".equals(className)) {
            className = "org.intermine.model.InterMineObject";
        } else {
            className = model.getPackageName() + "." + className;
        }
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiate a class by unqualified name
     * The name should be "Date" or that of a primitive container class such as "Integer"
     * @param className the name of the class
     * @return the relevant Class
     */
    public static Class getClass(String className) {
        Class cls = TypeUtil.instantiate(className);
        if (cls == null) {
            if ("Date".equals(className)) {
                cls = Date.class;
            } else {
                try {
                    cls = Class.forName("java.lang." + className);
                } catch (Exception e) {
                }
            }
        }
        return cls;
    }

    /**
     * Get the metadata for a class by unqualified name
     * The name is looked up in the provided model
     * @param className the name of the class
     * @param model the Model used to resolve class names
     * @return the relevant ClassDescriptor
     */
    public static ClassDescriptor getClassDescriptor(String className, Model model) {
        return model.getClassDescriptorByName(getClass(className, model).getName());
    }

    /**
     * Take a Collection of ConstraintOps and builds a map from ConstraintOp.getIndex() to
     * ConstraintOp.toString() for each
     * @param ops a Collection of ConstraintOps
     * @return the Map from index to string
     */
    public static Map mapOps(Collection ops) {
        Map opString = new LinkedHashMap();
        for (Iterator iter = ops.iterator(); iter.hasNext();) {
            ConstraintOp op = (ConstraintOp) iter.next();
            opString.put(op.getIndex(), op.toString());
        }
        return opString;
    }

    /**
     * Create constraint values for display. Returns a Map from Constraint to String
     * for each Constraint in the path query.
     *
     * @param pathquery  the PathQuery to look at
     * @return           Map from Constraint to displat value
     */
    public static Map makeConstraintDisplayMap(PathQuery pathquery) {
        Map map = new HashMap();
        Iterator iter = pathquery.getNodes().values().iterator();
        while (iter.hasNext()) {
            PathNode node = (PathNode) iter.next();
            Iterator citer = node.getConstraints().iterator();
            while (citer.hasNext()) {
                Constraint con = (Constraint) citer.next();
                ConstraintOp op = con.getOp();

                map.put(con, con.getDisplayValue(node));
            }
        }
        return map;
    }

    /**
     * Return the qualified name of the given unqualified class name.  The className must be in the
     * given model or in the java.lang package or one of java.util.Date or java.math.BigDecimal.
     * @param className the name of the class
     * @param model the Model used to resolve class names
     * @return the fully qualified name of the class
     * @throws ClassNotFoundException if the class can't be found
     */
    public static String getQualifiedTypeName(String className, Model model)
        throws ClassNotFoundException {

        if (className.indexOf(".") != -1) {
            throw new IllegalArgumentException("Expected an unqualified class name: " + className);
        }

        if (TypeUtil.instantiate(className) != null) {
            // a primative type
            return className;
        } else {
            if ("InterMineObject".equals(className)) {
                return "org.intermine.model.InterMineObject";
            } else {
                try {
                    return Class.forName(model.getPackageName() + "." + className).getName();
                } catch (ClassNotFoundException e) {
                    // fall through and try java.lang
                }
            }

            if ("Date".equals(className)) {
                return Date.class.getName();
            }

            if ("BigDecimal".equals(className)) {
                return BigDecimal.class.getName();
            }

            return Class.forName("java.lang." + className).getName();
        }
    }
    
    /**
     * Given a path, find out whether it represents an attribute or a reference/collection.
     * 
     * @param path the path
     * @param pathQuery the path query
     * @return true if path ends with an attribute, false if not
     */
    public static boolean isPathAttribute(String path, PathQuery pathQuery) {
        String classname = getTypeForPath(path, pathQuery);
        return !(classname.startsWith(pathQuery.getModel().getPackageName())
                || classname.endsWith("InterMineObject"));
    }
    
    /**
     * Return the fully qualified type of the last node in the given path.
     * @param path the path
     * @param pathQuery the PathQuery that contains the given path
     * @return the fully qualified type name
     * @throws IllegalArgumentException if the path isn't valid for the PathQuery or if any
     * arguments are null
     */
    public static String getTypeForPath(String path, PathQuery pathQuery) {
        // find the longest path that has a type stored in the pathQuery, then use the model to find
        // the type of the last node

        if (path == null) {
            throw new IllegalArgumentException("path argument cannot be null");
        }

        if (pathQuery == null) {
            throw new IllegalArgumentException("pathQuery argument cannot be null");
        }

        Model model = pathQuery.getModel();

        PathNode testPathNode = (PathNode) pathQuery.getNodes().get(path);
        if (testPathNode != null) {
            try {
                return getQualifiedTypeName(testPathNode.getType(), model);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("class \"" + testPathNode.getType()
                                                   + "\" not found");
            }
        }

        String[] bits = path.split("[.]");

        List bitsList = new ArrayList(Arrays.asList(bits));

        String prefix = null;

        while (bitsList.size() > 0) {
            prefix = StringUtil.join(bitsList, ".");
            if (pathQuery.getNodes().get(prefix) != null) {
                break;
            }

            bitsList.remove(bitsList.size() - 1);
        }

        // the longest path prefix that has an entry in the PathQuery
        String longestPrefix = prefix;

        ClassDescriptor cld;

        if (bitsList.size() == 0) {
            try {
                cld = model.getClassDescriptorByName(getQualifiedTypeName(bits[0], model));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("class \"" + bits[0] + "\" not found");
            }
        } else {
            PathNode pn = (PathNode) pathQuery.getNodes().get(longestPrefix);
            cld = getClassDescriptor(pn.getType(), model);
        }

        int startIndex = bitsList.size();

        if (startIndex < 1) {
            startIndex = 1;
        }

        for (int i = startIndex; i < bits.length; i++) {
            FieldDescriptor fd = cld.getFieldDescriptorByName(bits[i]);
            if (fd == null) {
                throw new IllegalArgumentException("could not find descriptor for: " + bits[i]);
            }
            if (fd.isAttribute()) {
                return ((AttributeDescriptor) fd).getType();
            } else {
                cld = ((ReferenceDescriptor) fd).getReferencedClassDescriptor();
            }
        }

        return cld.getName();
    }
}
