package org.intermine.objectstore;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCloner;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsInfo;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SingletonResults;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.objectstore.query.iql.IqlQuery;
import org.intermine.objectstore.query.iql.IqlQueryParser;
import org.intermine.util.DynamicUtil;

import org.intermine.model.testmodel.*;

/**
 * TestCase for all ObjectStores
 *
 */
public abstract class ObjectStoreTestCase extends StoreDataTestCase
{
    protected static ObjectStore os;

    public ObjectStoreTestCase(String arg) {
        super(arg);
    }

    public static void oneTimeSetUp() throws Exception {
        StoreDataTestCase.oneTimeSetUp();
        setUpResults();
    }

    /**
     * Set up all the results expected for a given subset of queries
     *
     * @throws Exception if an error occurs
     */
    public static void setUpResults() throws Exception {
        Object[][] r;

        r = new Object[][] { { data.get("CompanyA") },
                             { data.get("CompanyB") } };
        results.put("SelectSimpleObject", toList(r));

        r = new Object[][] { { "CompanyA", new Integer(5) },
                             { "CompanyB", new Integer(5) } };
        results.put("SubQuery", toList(r));

        r = new Object[][] { { "CompanyA" } };
        results.put("WhereSimpleEquals", toList(r));

        r = new Object[][] { { "CompanyB" } };
        results.put("WhereSimpleNotEquals", toList(r));
        results.put("WhereSimpleNegEquals", toList(r));

        r = new Object[][] { { "CompanyA" },
                             { "CompanyB" } };
        results.put("WhereSimpleLike", toList(r));

        r = new Object[][] { { "CompanyA" } };
        results.put("WhereEqualsString", toList(r));

        r = new Object[][] { { "CompanyB" } };
        results.put("WhereAndSet", toList(r));

        r = new Object[][] { { "CompanyA" },
                             { "CompanyB" } };
        results.put("WhereOrSet", toList(r));

        r = new Object[][] { { "CompanyA" } };
        results.put("WhereNotSet", toList(r));

        r = new Object[][] { { data.get("DepartmentA1") },
                             { data.get("DepartmentB1") },
                             { data.get("DepartmentB2") } };
        results.put("WhereSubQueryField", toList(r));

        r = new Object[][] { { data.get("CompanyA") } };
        results.put("WhereSubQueryClass", toList(r));

        r = new Object[][] { { data.get("CompanyB") } };
        results.put("WhereNotSubQueryClass", toList(r));

        r = new Object[][] { { data.get("CompanyB") } };
        results.put("WhereNegSubQueryClass", toList(r));

        r = new Object[][] { { data.get("CompanyA"), data.get("CompanyA") },
                             { data.get("CompanyB"), data.get("CompanyB") } };
        results.put("WhereClassClass", toList(r));

        r = new Object[][] { { data.get("CompanyA"), data.get("CompanyB") },
                             { data.get("CompanyB"), data.get("CompanyA") } };
        results.put("WhereNotClassClass", toList(r));

        r = new Object[][] { { data.get("CompanyA"), data.get("CompanyB") },
                             { data.get("CompanyB"), data.get("CompanyA") } };
        results.put("WhereNegClassClass", toList(r));

        r = new Object[][] { { data.get("CompanyA") } };
        results.put("WhereClassObject", toList(r));

        r = new Object[][] { { data.get("DepartmentA1"), data.get("EmployeeA1") } };
        results.put("Contains11", toList(r));

        r = new Object[][] { { data.get("DepartmentA1"), data.get("EmployeeB1") },
                             { data.get("DepartmentA1"), data.get("EmployeeB3") } };
        results.put("ContainsNot11", toList(r));
        results.put("ContainsNeg11", toList(r));

        r = new Object[][] { { data.get("CompanyA"), data.get("DepartmentA1") } };
        results.put("Contains1N", toList(r));

        r = new Object[][] { { data.get("DepartmentA1"), data.get("CompanyA") } };
        results.put("ContainsN1", toList(r));

        r = new Object[][] { { data.get("ContractorA"), data.get("CompanyA") },
                             { data.get("ContractorA"), data.get("CompanyB") } };
        results.put("ContainsMN", toList(r));

        r = new Object[][] { { data.get("ContractorA"), data.get("CompanyA") },
                             { data.get("ContractorA"), data.get("CompanyB") },
                             { data.get("ContractorB"), data.get("CompanyA") },
                             { data.get("ContractorB"), data.get("CompanyB") } };
        results.put("ContainsDuplicatesMN", toList(r));

        r = new Object[][] { { data.get("DepartmentA1") } };
        results.put("ContainsObject", toList(r));

        r = new Object[][] { { data.get("CompanyA"), new Long(1) },
                             { data.get("CompanyB"), new Long(2) } };
        results.put("SimpleGroupBy", toList(r));

        r = new Object[][] { { data.get("CompanyA"), data.get("DepartmentA1"), data.get("EmployeeA1"), ((Employee)data.get("EmployeeA1")).getAddress() } };
        results.put("MultiJoin", toList(r));

        r = new Object[][] { { new BigDecimal("3476.0000000000000000"), "DepartmentA1", data.get("DepartmentA1") },
                             { new BigDecimal("3476.0000000000000000"), "DepartmentB1", data.get("DepartmentB1") },
                             { new BigDecimal("3476.0000000000000000"), "DepartmentB2", data.get("DepartmentB2") } };
        results.put("SelectComplex", toList(r));

        r = new Object[][] { { data.get("EmployeeA1") },
                             { data.get("EmployeeA2") },
                             { data.get("EmployeeA3") },
                             { data.get("EmployeeB1") },
                             { data.get("EmployeeB2") },
                             { data.get("EmployeeB3") } };
        results.put("SelectClassAndSubClasses", toList(r));

        r = new Object[][] { { data.get("ContractorA") },
                             { data.get("ContractorB") },
                             { data.get("EmployeeB1") },
                             { data.get("EmployeeA1") },
                             { data.get("EmployeeA2") },
                             { data.get("EmployeeA3") },
                             { data.get("EmployeeB2") },
                             { data.get("EmployeeB3") } };
        results.put("SelectInterfaceAndSubClasses", toList(r));

        r = new Object[][] { { data.get("CompanyA") },
                             { data.get("DepartmentA1") },
                             { data.get("CompanyB") },
                             { data.get("DepartmentB1") },
                             { data.get("DepartmentB2") } };
        results.put("SelectInterfaceAndSubClasses2", toList(r));

        r = new Object[][] { { data.get("ContractorA") },
                             { data.get("ContractorB") },
                             { data.get("EmployeeB1") },
                             { data.get("EmployeeA1") },
                             { data.get("EmployeeB3") } };
        results.put("SelectInterfaceAndSubClasses3", toList(r));

        r = new Object[][] { { new Integer(5), "CompanyA" },
                             { new Integer(5), "CompanyB" } };
        results.put("OrderByAnomaly", toList(r));

        r = new Object[][] { { data.get("Secretary1") },
                             { data.get("Secretary2") },
                             { data.get("Secretary3") } };
        results.put("SelectUnidirectionalCollection", toList(r));

        r = new Object[][] { { data.get("CompanyA") } };
        results.put("SelectClassObjectSubquery", toList(r));

        r = new Object[][] { { data.get("CompanyA") },
                             { data.get("CompanyB") } };
        results.put("EmptyAndConstraintSet", toList(r));

        results.put("EmptyOrConstraintSet", Collections.EMPTY_LIST);

        results.put("EmptyNandConstraintSet", Collections.EMPTY_LIST);

        results.put("EmptyNorConstraintSet", toList(r));

        results.put("BagConstraint", Collections.singletonList(Collections.singletonList(data.get("CompanyA"))));

        results.put("BagConstraint2", Collections.singletonList(Collections.singletonList(data.get("CompanyA"))));

        r = new Object[][] { { data.get("EmployeeA1") } };
        results.put("InterfaceField", toList(r));

        r = new Object[][] { { data.get("EmployeeA1") },
                             { data.get("EmployeeA2") },
                             { data.get("EmployeeA3") } };
        results.put("InterfaceReference", toList(r));

        r = new Object[][] { { data.get("CompanyA") },
                             { data.get("CompanyB") } };
        results.put("InterfaceCollection", toList(r));

        r = new Object[][] { { data.get("CompanyA"), new Integer(876324), new Integer(1234) } };
        results.put("DynamicInterfacesAttribute", toList(r));

        r = new Object[][] { { data.get("ContractorA") },
                             { data.get("EmployeeB1") } };
        results.put("DynamicClassInterface", toList(r));

        results.put("DynamicClassRef1", Collections.EMPTY_LIST);
        results.put("DynamicClassRef2", Collections.EMPTY_LIST);
        results.put("DynamicClassRef3", Collections.EMPTY_LIST);
        results.put("DynamicClassRef4", Collections.EMPTY_LIST);

        r = new Object[][] { { data.get("EmployeeB1") } };
        results.put("DynamicClassConstraint", toList(r));

        r = new Object[][] { { data.get("EmployeeB1") } };
        results.put("ContainsConstraintNull", toList(r));

        r = new Object[][] { { data.get("EmployeeA1") },
                             { data.get("EmployeeA2") },
                             { data.get("EmployeeA3") },
                             { data.get("EmployeeB2") },
                             { data.get("EmployeeB3") } };
        results.put("ContainsConstraintNotNull", toList(r));

        r = new Object[][] { { data.get("EmployeeA1") },
                             { data.get("EmployeeB3") } };
        results.put("SimpleConstraintNull", toList(r));

        r = new Object[][] { { data.get("EmployeeB1") } };
        results.put("SimpleConstraintNotNull", toList(r));

        r = new Object[][] { { "10" },
            { "20" },
            { "30" },
            { "40" },
            { "50" },
            { "60" } };
        results.put("TypeCast", toList(r));

        r = new Object[][] { { new Integer(5) },
            { new Integer(5) },
            { new Integer(5) },
            { new Integer(5) },
            { new Integer(5) },
            { new Integer(5) } };
        results.put("IndexOf", toList(r));

        r = new Object[][] { { "mp" },
            { "mp" },
            { "mp" },
            { "mp" },
            { "mp" },
            { "mp" } };
        results.put("Substring", toList(r));

        r = new Object[][] { { "mployeeA1" },
            { "mployeeA2" },
            { "mployeeA3" },
            { "mployeeB1" },
            { "mployeeB2" },
            { "mployeeB3" } };
        results.put("Substring2", toList(r));

        r = new Object[][] { { data.get("EmployeeA1") },
                             { data.get("EmployeeA2") },
                             { data.get("EmployeeA3") },
                             { data.get("EmployeeB1") },
                             { data.get("EmployeeB2") },
                             { data.get("EmployeeB3") } };
        results.put("OrderByReference", toList(r));
    }

    /**
     * Execute a test for a query. This should run the query and
     * contain an assert call to assert that the returned results are
     * those expected.
     *
     * @param type the type of query we are testing (ie. the key in the queries Map)
     * @throws Exception if type does not appear in the queries map
     */
    public void executeTest(String type) throws Exception {
        Results res = os.execute((Query)queries.get(type));
        assertEquals(type + " has failed", results.get(type), res);
    }

    public void testResults() throws Exception {
        Object[][] r = new Object[][] { { data.get("CompanyA") },
                                        { data.get("CompanyB") } };
        List res = os.execute((Query) queries.get("SelectSimpleObject"));
        assertEquals(toList(r).size(), res.size());
        assertEquals(toList(r), res);
    }

    // estimate tests

    public void testEstimateQueryNotNull() throws Exception {
        ResultsInfo er = os.estimate((Query)queries.get("WhereClassClass"));
        if (er == null) {
            fail("a null ResultsInfo was returned");
        }
    }

    // reference and collection proxy tests

    public void testCEOWhenSearchingForManager() throws Exception {
        // select manager where manager.name="EmployeeB1" (actually a CEO)
        QueryClass c1 = new QueryClass(Manager.class);
        Query q1 = new Query();
        q1.addFrom(c1);
        q1.addToSelect(c1);
        QueryField f1 = new QueryField(c1, "name");
        QueryValue v1 = new QueryValue("EmployeeB1");
        SimpleConstraint sc1 = new SimpleConstraint(f1, ConstraintOp.EQUALS, v1);
        q1.setConstraint(sc1);
        List l1 = os.execute(q1);
        CEO ceo = (CEO) (((ResultsRow) l1.get(0)).get(0));
        assertEquals(45000, ceo.getSalary());
    }

    public void testLazyCollection() throws Exception {
        List r = os.execute((Query) queries.get("ContainsN1"));
        Department d = (Department) ((ResultsRow) r.get(0)).get(0);
        assertTrue("Expected " + d.getEmployees().getClass() + " to be a SingletonResults object", d.getEmployees() instanceof SingletonResults);

        Set expected = new HashSet();
        expected.add(data.get("EmployeeA1"));
        expected.add(data.get("EmployeeA2"));
        expected.add(data.get("EmployeeA3"));
        assertEquals(expected, new HashSet(d.getEmployees()));
    }

    public void testLazyCollectionMtoN() throws Exception {
        // query for company and check contractors
        QueryClass c1 = new QueryClass(Company.class);
        Query q1 = new Query();
        q1.addFrom(c1);
        q1.addToSelect(c1);
        QueryField f1 = new QueryField(c1, "name");
        QueryValue v1 = new QueryValue("CompanyA");
        SimpleConstraint sc1 = new SimpleConstraint(f1, ConstraintOp.EQUALS, v1);
        q1.setConstraint(sc1);
        Results r  = os.execute(q1);
        ResultsRow rr = (ResultsRow) r.get(0);
        Company c = (Company) rr.get(0);
        assertTrue("Expected " + c.getContractors().getClass() + " to be a SingletonResults object", c.getContractors() instanceof SingletonResults);
        Set contractors = new HashSet(c.getContractors());
        Set expected1 = new HashSet();
        expected1.add(data.get("ContractorA"));
        expected1.add(data.get("ContractorB"));
        assertEquals(expected1, contractors);

        Contractor contractor1 = (Contractor) contractors.iterator().next();
        assertTrue("Expected " + contractor1.getCompanys().getClass() + " to be a SingletonResults object", contractor1.getCompanys() instanceof SingletonResults);
        Set expected2 = new HashSet();
        expected2.add(data.get("CompanyA"));
        expected2.add(data.get("CompanyB"));
        assertEquals(expected2, new HashSet(contractor1.getCompanys()));
    }

    // setDistinct tests

    public void testCountNoGroupByNotDistinct() throws Exception {
        Query q = QueryCloner.cloneQuery((Query) queries.get("ContainsDuplicatesMN"));
        q.setDistinct(false);
        int count = os.count(q, os.getSequence());
        assertEquals(4, count);
    }

    public void testCountNoGroupByDistinct() throws Exception {
        Query q = (Query) queries.get("ContainsDuplicatesMN");
        int count = os.count(q, os.getSequence());
        assertEquals(4, os.execute(q).size());
    }

   public void testCountGroupByNotDistinct() throws Exception {
        Query q = QueryCloner.cloneQuery((Query) queries.get("SimpleGroupBy"));
        q.setDistinct(false);
        int count = os.count(q, os.getSequence());
        assertEquals(2, count);
    }

    public void testCountGroupByDistinct() throws Exception {
    // distinct doesn't actually do anything to group by reuslt
        Query q = (Query) queries.get("SimpleGroupBy");
        int count = os.count(q, os.getSequence());
        assertEquals(2, count);
    }

    // getObjectByExample tests

    public void testGetObjectByExampleNull() throws Exception {
        try {
            os.getObjectByExample(null, Collections.singleton("name"));
            fail("Expected: NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetObjectByExampleNonExistent() throws Exception {
        Address a = new Address();
        a.setAddress("10 Downing Street");
        assertNull(os.getObjectByExample(a, Collections.singleton("address")));
    }

    public void testGetObjectByExampleAttribute() throws Exception {
        Address a1 = ((Employee) data.get("EmployeeA1")).getAddress();
        Address a = new Address();
        a.setAddress(a1.getAddress());
        assertEquals(a1, os.getObjectByExample(a, Collections.singleton("address")));
    }

    public void testGetObjectByExampleFields() throws Exception {
        Employee e1 = (Employee) data.get("EmployeeA1");
        Employee e = new Employee();
        e.setName(e1.getName());
        e.setAge(e1.getAge());
        e.setAddress(e1.getAddress());
        assertEquals(e1, os.getObjectByExample(e, new HashSet(Arrays.asList(new String[] {"name", "age", "address"}))));
    }

    public void testDataTypes() throws Exception {
        Types d1 = (Types) data.get("Types1");
        //Types d2 = new Types();
        //d2.setName(d1.getName());
        Query q = new Query();
        QueryClass c = new QueryClass(Types.class);
        q.addFrom(c);
        q.addToSelect(c);
        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
        QueryField name = new QueryField(c, "name");
        QueryField booleanType = new QueryField(c, "booleanType");
        QueryField floatType = new QueryField(c, "floatType");
        QueryField doubleType = new QueryField(c, "doubleType");
        QueryField shortType = new QueryField(c, "shortType");
        QueryField intType = new QueryField(c, "intType");
        QueryField longType = new QueryField(c, "longType");
        QueryField booleanObjType = new QueryField(c, "booleanObjType");
        QueryField floatObjType = new QueryField(c, "floatObjType");
        QueryField doubleObjType = new QueryField(c, "doubleObjType");
        QueryField shortObjType = new QueryField(c, "shortObjType");
        QueryField intObjType = new QueryField(c, "intObjType");
        QueryField longObjType = new QueryField(c, "longObjType");
        QueryField bigDecimalObjType = new QueryField(c, "bigDecimalObjType");
        QueryField stringObjType = new QueryField(c, "stringObjType");
        QueryField dateObjType = new QueryField(c, "dateObjType");
        q.addToSelect(name);
        q.addToSelect(booleanType);
        q.addToSelect(floatType);
        q.addToSelect(doubleType);
        q.addToSelect(shortType);
        q.addToSelect(intType);
        q.addToSelect(longType);
        q.addToSelect(booleanObjType);
        q.addToSelect(floatObjType);
        q.addToSelect(doubleObjType);
        q.addToSelect(shortObjType);
        q.addToSelect(intObjType);
        q.addToSelect(longObjType);
        q.addToSelect(bigDecimalObjType);
        q.addToSelect(stringObjType);
        q.addToSelect(dateObjType);
        cs.addConstraint(new SimpleConstraint(name, ConstraintOp.EQUALS, new QueryValue("Types1")));
        cs.addConstraint(new SimpleConstraint(booleanType, ConstraintOp.EQUALS, new QueryValue(Boolean.TRUE)));
        cs.addConstraint(new SimpleConstraint(floatType, ConstraintOp.EQUALS, new QueryValue(new Float(0.6F))));
        cs.addConstraint(new SimpleConstraint(doubleType, ConstraintOp.EQUALS, new QueryValue(new Double(0.88D))));
        cs.addConstraint(new SimpleConstraint(shortType, ConstraintOp.EQUALS, new QueryValue(new Short((short) 675))));
        cs.addConstraint(new SimpleConstraint(intType, ConstraintOp.EQUALS, new QueryValue(new Integer(267))));
        cs.addConstraint(new SimpleConstraint(longType, ConstraintOp.EQUALS, new QueryValue(new Long(98729353495843l))));
        cs.addConstraint(new SimpleConstraint(booleanObjType, ConstraintOp.EQUALS, new QueryValue(Boolean.TRUE)));
        cs.addConstraint(new SimpleConstraint(floatObjType, ConstraintOp.EQUALS, new QueryValue(new Float(1.6F))));
        cs.addConstraint(new SimpleConstraint(doubleObjType, ConstraintOp.EQUALS, new QueryValue(new Double(1.88D))));
        cs.addConstraint(new SimpleConstraint(shortObjType, ConstraintOp.EQUALS, new QueryValue(new Short((short) 1982))));
        cs.addConstraint(new SimpleConstraint(intObjType, ConstraintOp.EQUALS, new QueryValue(new Integer(369))));
        cs.addConstraint(new SimpleConstraint(longObjType, ConstraintOp.EQUALS, new QueryValue(new Long(38762874323212l))));
        cs.addConstraint(new SimpleConstraint(bigDecimalObjType, ConstraintOp.EQUALS, new QueryValue(new BigDecimal("876323428764587621764532432.8768173432887324123645"))));
        cs.addConstraint(new SimpleConstraint(stringObjType, ConstraintOp.EQUALS, new QueryValue("A test String")));
        cs.addConstraint(new SimpleConstraint(dateObjType, ConstraintOp.EQUALS, new QueryValue(new Date(7777777l))));

        q.setConstraint(cs);
        Results res = os.execute(q);
        List row1 = (List) res.get(0);
        Types d = (Types) row1.get(0);

        //Types d = (Types) (os.getObjectByExample(d2));

        // Go through each attribute to check that it has been set correctly
        assertEquals(d1.getName(), d.getName());
        assertEquals(d1.getName(), row1.get(1));
        assertEquals(d1.getBooleanType(), d.getBooleanType());
        assertEquals(Boolean.class, row1.get(2).getClass());
        assertEquals(d1.getBooleanType(), ((Boolean) row1.get(2)).booleanValue());
        assertEquals(d1.getFloatType(), d.getFloatType(), 0.0);
        assertEquals(Float.class, row1.get(3).getClass());
        assertEquals(d1.getFloatType(), ((Float) row1.get(3)).floatValue(), 0.0);
        assertEquals(d1.getDoubleType(), d.getDoubleType(), 0.0);
        assertEquals(Double.class, row1.get(4).getClass());
        assertEquals(d1.getDoubleType(), ((Double) row1.get(4)).doubleValue(), 0.0);
        assertEquals(d1.getShortType(), d.getShortType());
        assertEquals(Short.class, row1.get(5).getClass());
        assertEquals(d1.getShortType(), ((Short) row1.get(5)).shortValue());
        assertEquals(d1.getIntType(), d.getIntType());
        assertEquals(Integer.class, row1.get(6).getClass());
        assertEquals(d1.getIntType(), ((Integer) row1.get(6)).intValue());
        assertEquals(d1.getLongType(), d.getLongType());
        assertEquals(Long.class, row1.get(7).getClass());
        assertEquals(d1.getLongType(), ((Long) row1.get(7)).longValue());
        assertEquals(d1.getBooleanObjType(), d.getBooleanObjType());
        assertEquals(d1.getBooleanObjType(), row1.get(8));
        assertEquals(d1.getFloatObjType(), d.getFloatObjType());
        assertEquals(d1.getFloatObjType(), row1.get(9));
        assertEquals(d1.getDoubleObjType(), d.getDoubleObjType());
        assertEquals(d1.getDoubleObjType(), row1.get(10));
        assertEquals(d1.getShortObjType(), d.getShortObjType());
        assertEquals(d1.getShortObjType(), row1.get(11));
        assertEquals(d1.getIntObjType(), d.getIntObjType());
        assertEquals(d1.getIntObjType(), row1.get(12));
        assertEquals(d1.getLongObjType(), d.getLongObjType());
        assertEquals(d1.getLongObjType(), row1.get(13));
        assertEquals(d1.getBigDecimalObjType(), d.getBigDecimalObjType());
        assertEquals(d1.getBigDecimalObjType(), row1.get(14));
        assertEquals(d1.getStringObjType(), d.getStringObjType());
        assertEquals(d1.getStringObjType(), row1.get(15));
        assertEquals(d1.getDateObjType(), d.getDateObjType());
        assertEquals(Date.class, row1.get(16).getClass());
        assertEquals(d1.getDateObjType(), row1.get(16));
    }

    public void testGetObjectByNullId() throws Exception {
        try {
            os.getObjectById(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetObjectById() throws Exception {
        Integer id = ((Employee) data.get("EmployeeA1")).getId();
        Employee e = (Employee) os.getObjectById(id);
        assertEquals(data.get("EmployeeA1"), e);
        assertTrue(e == os.getObjectById(id));
    }

    public void testGetObjectMultipleTimes() throws Exception {
        Query q = IqlQueryParser.parse(new IqlQuery("select Secretary from Secretary where Secretary.name = 'Secretary1'", "org.intermine.model.testmodel"));
        Secretary a = (Secretary) ((List) os.execute(q).get(0)).get(0);

        Secretary b = (Secretary) os.getObjectById(a.getId());
        Secretary c = (Secretary) os.getObjectById(a.getId());
        assertEquals(b, c);
        assertTrue(b == c);
        assertEquals(a, b);
        assertTrue(a == b);
    }

    public void testIndirectionTableMultipleCopies() throws Exception {
        Contractor c1 = new Contractor();
        c1.setName("Clippy");
        Company c2 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        c2.setName("�bersoft");
        c2.addContractors(c1);
        c1.addCompanys(c2);

        storeDataWriter.store(c1);
        storeDataWriter.store(c2);

        Query q1 = new Query();
        QueryClass qc1 = new QueryClass(Contractor.class);
        QueryClass qc2 = new QueryClass(Company.class);
        q1.addFrom(qc1);
        q1.addFrom(qc2);
        q1.addToSelect(qc1);
        q1.addToSelect(qc2);
        SimpleConstraint sc = new SimpleConstraint(new QueryField(qc1, "name"), ConstraintOp.EQUALS, new QueryValue("Clippy"));
        ContainsConstraint cc = new ContainsConstraint(new QueryCollectionReference(qc1, "companys"), ConstraintOp.CONTAINS, qc2);
        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
        cs.addConstraint(sc);
        cs.addConstraint(cc);
        q1.setConstraint(cs);
        q1.setDistinct(false);

        try {
            Results r1 = os.execute(q1);
            assertEquals(1, r1.size());
            
            storeDataWriter.store(c1);
            Results r2 = os.execute(q1);
            assertEquals(1, r1.size());
        } finally {
            storeDataWriter.delete(c1);
            storeDataWriter.delete(c2);
        }
    }

    // helper method

    protected static List toList(Object[][] o) {
        List rows = new ArrayList();
        for(int i=0;i<o.length;i++) {
            rows.add(new ResultsRow(Arrays.asList((Object[])o[i])));
        }
        return rows;
    }
}
