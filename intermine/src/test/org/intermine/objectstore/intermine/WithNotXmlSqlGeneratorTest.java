package org.intermine.objectstore.intermine;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.intermine.testing.OneTimeTestCase;
import org.intermine.sql.DatabaseFactory;
import org.intermine.util.TypeUtil;

public class WithNotXmlSqlGeneratorTest extends SqlGeneratorTest
{
    public WithNotXmlSqlGeneratorTest(String arg) {
        super(arg);
    }

    public static Test suite() {
        return OneTimeTestCase.buildSuite(WithNotXmlSqlGeneratorTest.class);
    }

    public static void oneTimeSetUp() throws Exception {
        SqlGeneratorTest.oneTimeSetUp();
        setUpResults();
        db = DatabaseFactory.getDatabase("db.notxmlunittest");
    }

    public static void setUpResults() throws Exception {
        results.put("SelectSimpleObject", "SELECT intermine_Alias.OBJECT AS \"intermine_Alias\", intermine_Alias.id AS \"intermine_Aliasid\" FROM Company AS intermine_Alias ORDER BY intermine_Alias.id");
        results2.put("SelectSimpleObject", Collections.singleton("Company"));
        results.put("SubQuery", "SELECT DISTINCT intermine_All.intermine_Arrayname AS a1_, intermine_All.intermine_Alias AS \"intermine_Alias\" FROM (SELECT intermine_Array.OBJECT AS intermine_Array, intermine_Array.CEOId AS intermine_ArrayCEOId, intermine_Array.addressId AS intermine_ArrayaddressId, intermine_Array.id AS intermine_Arrayid, intermine_Array.name AS intermine_Arrayname, intermine_Array.vatNumber AS intermine_ArrayvatNumber, 5 AS intermine_Alias FROM Company AS intermine_Array) AS intermine_All ORDER BY intermine_All.intermine_Arrayname, intermine_All.intermine_Alias");
        results2.put("SubQuery", Collections.singleton("Company"));
        results.put("WhereSimpleEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber = 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleEquals", Collections.singleton("Company"));
        results.put("WhereSimpleNotEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber != 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleNotEquals", Collections.singleton("Company"));
        results.put("WhereSimpleNegEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber != 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleNegEquals", Collections.singleton("Company"));
        results.put("WhereSimpleLike", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.name LIKE 'Company%' ORDER BY a1_.name");
        results2.put("WhereSimpleLike", Collections.singleton("Company"));
        results.put("WhereEqualsString", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.name = 'CompanyA' ORDER BY a1_.name");
        results2.put("WhereEqualsString", Collections.singleton("Company"));
        results.put("WhereAndSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000) ORDER BY a1_.name");
        results2.put("WhereAndSet", Collections.singleton("Company"));
        results.put("WhereOrSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE (a1_.name LIKE 'CompanyA%' OR a1_.vatNumber > 2000) ORDER BY a1_.name");
        results2.put("WhereOrSet", Collections.singleton("Company"));
        results.put("WhereNotSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE ( NOT (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000)) ORDER BY a1_.name");
        results2.put("WhereNotSet", Collections.singleton("Company"));
        results.put("WhereSubQueryField", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM Department AS a1_ WHERE a1_.name IN (SELECT DISTINCT a1_.name FROM Department AS a1_) ORDER BY a1_.name, a1_.id");
        results2.put("WhereSubQueryField", Collections.singleton("Department"));
        results.put("WhereSubQueryClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id IN (SELECT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereSubQueryClass", Collections.singleton("Company"));
        results.put("WhereNotSubQueryClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id NOT IN (SELECT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereNotSubQueryClass", Collections.singleton("Company"));
        results.put("WhereNegSubQueryClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id NOT IN (SELECT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereNegSubQueryClass", Collections.singleton("Company"));
        results.put("WhereClassClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id = a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereClassClass", Collections.singleton("Company"));
        results.put("WhereNotClassClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereNotClassClass", Collections.singleton("Company"));
        results.put("WhereNegClassClass", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereNegClassClass", Collections.singleton("Company"));
        Integer id1 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        results.put("WhereClassObject", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id = " + id1 + " ORDER BY a1_.id");
        results2.put("WhereClassObject", Collections.singleton("Company"));
        results.put("Contains11", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId = a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("Contains11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("ContainsNot11", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsNot11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("ContainsNeg11", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsNeg11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("Contains1N", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Department AS a2_ WHERE (a1_.id = a2_.companyId AND a1_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results2.put("Contains1N", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("ContainsN1", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Company AS a2_ WHERE (a1_.companyId = a2_.id AND a2_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsN1", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("ContainsMN", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Contractor AS a1_, Company AS a2_, CompanysContractors AS indirect0 WHERE ((a1_.id = indirect0.Companys AND indirect0.Contractors = a2_.id) AND a1_.name = 'ContractorA') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsMN", new HashSet(Arrays.asList(new String[] {"Contractor", "Company", "CompanysContractors"})));
        results.put("ContainsDuplicatesMN", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Contractor AS a1_, Company AS a2_, OldComsOldContracts AS indirect0 WHERE (a1_.id = indirect0.OldComs AND indirect0.OldContracts = a2_.id) ORDER BY a1_.id, a2_.id");
        results2.put("ContainsDuplicatesMN", new HashSet(Arrays.asList(new String[] {"Contractor", "Company", "OldComsOldContracts"})));
        id1 = (Integer) TypeUtil.getFieldValue(data.get("EmployeeA1"), "id");
        results.put("ContainsObject", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Department AS a1_ WHERE a1_.managerId = " + id1 + " ORDER BY a1_.id");
        results2.put("ContainsObject", Collections.singleton("Department"));
        results.put("SimpleGroupBy", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, COUNT(*) AS a2_ FROM Company AS a1_, Department AS a3_ WHERE a1_.id = a3_.companyId GROUP BY a1_.OBJECT, a1_.CEOId, a1_.addressId, a1_.id, a1_.name, a1_.vatNumber ORDER BY a1_.id, COUNT(*)");
        results2.put("SimpleGroupBy", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("MultiJoin", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id, a4_.OBJECT AS a4_, a4_.id AS a4_id FROM Company AS a1_, Department AS a2_, Manager AS a3_, Address AS a4_ WHERE (a1_.id = a2_.companyId AND a2_.managerId = a3_.id AND a3_.addressId = a4_.id AND a3_.name = 'EmployeeA1') ORDER BY a1_.id, a2_.id, a3_.id, a4_.id");
        results2.put("MultiJoin", new HashSet(Arrays.asList(new String[] {"Department", "Manager", "Company", "Address"})));
        results.put("SelectComplex", "SELECT DISTINCT (AVG(a1_.vatNumber) + 20) AS a3_, a2_.name AS a4_, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Department AS a2_ GROUP BY a2_.OBJECT, a2_.companyId, a2_.id, a2_.managerId, a2_.name ORDER BY (AVG(a1_.vatNumber) + 20), a2_.name, a2_.id");
        results2.put("SelectComplex", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("SelectClassAndSubClasses", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM Employee AS a1_ ORDER BY a1_.name, a1_.id");
        results2.put("SelectClassAndSubClasses", Collections.singleton("Employee"));
        results.put("SelectInterfaceAndSubClasses", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses", Collections.singleton("Employable"));
        results.put("SelectInterfaceAndSubClasses2", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM RandomInterface AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses2", Collections.singleton("RandomInterface"));
        results.put("SelectInterfaceAndSubClasses3", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM ImportantPerson AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses3", Collections.singleton("ImportantPerson"));
        results.put("OrderByAnomaly", "SELECT DISTINCT 5 AS a2_, a1_.name AS a3_ FROM Company AS a1_ ORDER BY a1_.name");
        results2.put("OrderByAnomaly", Collections.singleton("Company"));
        Integer id2 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        Integer id3 = (Integer) TypeUtil.getFieldValue(data.get("DepartmentA1"), "id");
        results.put("SelectClassObjectSubquery", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_, Department AS a2_ WHERE (a1_.id = " + id2 + " AND a1_.id = a2_.companyId AND a2_.id IN (SELECT a1_.id FROM Department AS a1_ WHERE a1_.id = " + id3 + ")) ORDER BY a1_.id");
        results2.put("SelectClassObjectSubquery", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("SelectUnidirectionalCollection", "SELECT DISTINCT a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Secretary AS a2_, HasSecretarysSecretarys AS indirect0 WHERE (a1_.name = 'CompanyA' AND (a1_.id = indirect0.Secretarys AND indirect0.HasSecretarys = a2_.id)) ORDER BY a2_.id");
        results2.put("SelectUnidirectionalCollection", new HashSet(Arrays.asList(new String[] {"Company", "Secretary", "HasSecretarysSecretarys"})));
        results.put("EmptyAndConstraintSet", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE true ORDER BY a1_.id");
        results2.put("EmptyAndConstraintSet", Collections.singleton("Company"));
        results.put("EmptyOrConstraintSet", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE false ORDER BY a1_.id");
        results2.put("EmptyOrConstraintSet", Collections.singleton("Company"));
        results.put("EmptyNandConstraintSet", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE false ORDER BY a1_.id");
        results2.put("EmptyNandConstraintSet", Collections.singleton("Company"));
        results.put("EmptyNorConstraintSet", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE true ORDER BY a1_.id");
        results2.put("EmptyNorConstraintSet", Collections.singleton("Company"));
        results.put("BagConstraint", "SELECT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM Company AS Company WHERE (Company.name IN ('CompanyA', 'goodbye', 'hello')) ORDER BY Company.id");
        results2.put("BagConstraint", Collections.singleton("Company"));
        results.put("BagConstraint2", "SELECT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM Company AS Company WHERE (Company.id IN (" + id2 + ")) ORDER BY Company.id");
        results2.put("BagConstraint2", Collections.singleton("Company"));
        results.put("InterfaceField", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_ WHERE a1_.name = 'EmployeeA1' ORDER BY a1_.id");
        results2.put("InterfaceField", Collections.singleton("Employable"));
        results.put("InterfaceReference", NO_RESULT);
        results.put("InterfaceCollection", NO_RESULT);
        Set res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1__1.debt AS a2_, a1_.age AS a3_ FROM Employee AS a1_, Broke AS a1__1 WHERE a1_.id = a1__1.id AND (a1__1.debt > 0 AND a1_.age > 0) ORDER BY a1_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.debt AS a2_, a1__1.age AS a3_ FROM Broke AS a1_, Employee AS a1__1 WHERE a1_.id = a1__1.id AND (a1_.debt > 0 AND a1__1.age > 0) ORDER BY a1_.id");
        results.put("DynamicInterfacesAttribute", res);
        results2.put("DynamicInterfacesAttribute", new HashSet(Arrays.asList(new String[] {"Employee", "Broke"})));
        res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1 WHERE a1_.id = a1__1.id ORDER BY a1_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1 WHERE a1_.id = a1__1.id ORDER BY a1_.id");
        results.put("DynamicClassInterface", res);
        results2.put("DynamicClassInterface", new HashSet(Arrays.asList(new String[] {"Employable", "Broke"})));
        res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Department AS a1_, Broke AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a2_.id = a1_.companyId AND a3_.id = a1__1.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Broke AS a1_, Department AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a2_.id = a1__1.companyId AND a3_.id = a1_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef1", res);
        results2.put("DynamicClassRef1", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Department AS a1_, Broke AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a1_.companyId = a2_.id AND a1__1.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Broke AS a1_, Department AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a1__1.companyId = a2_.id AND a1_.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef2", res);
        results2.put("DynamicClassRef2", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Company AS a1_, Bank AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Bank AS a1_, Company AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef3", res);
        results2.put("DynamicClassRef3", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Company AS a1_, Bank AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Bank AS a1_, Company AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef4", res);
        results2.put("DynamicClassRef4", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1, HasAddress AS a2_, Broke AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1, Broke AS a2_, HasAddress AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1, HasAddress AS a2_, Broke AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1, Broke AS a2_, HasAddress AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        results.put("DynamicClassConstraint", res);
        results2.put("DynamicClassConstraint", new HashSet(Arrays.asList(new String[] {"Employable", "Broke", "HasAddress"})));
        results.put("ContainsConstraintNull", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.addressId IS NULL ORDER BY a1_.id");
        results2.put("ContainsConstraintNull", Collections.singleton("Employee"));
        results.put("ContainsConstraintNotNull", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.addressId IS NOT NULL ORDER BY a1_.id");
        results2.put("ContainsConstraintNotNull", Collections.singleton("Employee"));
        results.put("SimpleConstraintNull", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Manager AS a1_ WHERE a1_.title IS NULL ORDER BY a1_.id");
        results2.put("SimpleConstraintNull", Collections.singleton("Manager"));
        results.put("SimpleConstraintNotNull", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Manager AS a1_ WHERE a1_.title IS NOT NULL ORDER BY a1_.id");
        results2.put("SimpleConstraintNotNull", Collections.singleton("Manager"));
        results.put("TypeCast", "SELECT DISTINCT (a1_.age)::TEXT AS a2_ FROM Employee AS a1_ ORDER BY (a1_.age)::TEXT");
        results2.put("TypeCast", Collections.singleton("Employee"));
        results.put("IndexOf", "SELECT STRPOS(a1_.name, 'oy') AS a2_ FROM Employee AS a1_ ORDER BY STRPOS(a1_.name, 'oy')");
        results2.put("IndexOf", Collections.singleton("Employee"));
        results.put("Substring", "SELECT SUBSTR(a1_.name, 2, 2) AS a2_ FROM Employee AS a1_ ORDER BY SUBSTR(a1_.name, 2, 2)");
        results2.put("Substring", Collections.singleton("Employee"));
        results.put("Substring2", "SELECT SUBSTR(a1_.name, 2) AS a2_ FROM Employee AS a1_ ORDER BY SUBSTR(a1_.name, 2)");
        results2.put("Substring2", Collections.singleton("Employee"));
        results.put("OrderByReference", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.departmentId AS orderbyfield0 FROM Employee AS a1_ ORDER BY a1_.departmentId, a1_.id");
        results2.put("OrderByReference", Collections.singleton("Employee"));

        String largeBagConstraintText = new BufferedReader(new InputStreamReader(TruncatedSqlGeneratorTest.class.getClassLoader().getResourceAsStream("test/withNotXmlLargeBag.sql"))).readLine();
        results.put("LargeBagConstraint", largeBagConstraintText);
        results2.put("LargeBagConstraint", Collections.singleton("Employee"));

        String largeNotBagConstraintText = new BufferedReader(new InputStreamReader(TruncatedSqlGeneratorTest.class.getClassLoader().getResourceAsStream("test/withNotXmlLargeNotBag.sql"))).readLine();
        results.put("LargeBagNotConstraint", largeNotBagConstraintText);
        results2.put("LargeBagNotConstraint", Collections.singleton("Employee"));

        results.put("LargeBagConstraintUsingTable", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.name IN (SELECT value FROM " + LARGE_BAG_TABLE_NAME + ") ORDER BY a1_.id");
        results2.put("LargeBagConstraintUsingTable", Collections.singleton("Employee"));

        results.put("LargeBagNotConstraintUsingTable", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE NOT (a1_.name IN (SELECT value FROM " + LARGE_BAG_TABLE_NAME + ")) ORDER BY a1_.id");
        results2.put("LargeBagNotConstraintUsingTable", Collections.singleton("Employee"));

        results.put("NegativeNumbers", "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.age > -51 ORDER BY a1_.id");
        results2.put("NegativeNumbers", Collections.singleton("Employee"));

        results.put("Lower", "SELECT LOWER(a1_.name) AS a2_ FROM Employee AS a1_ ORDER BY LOWER(a1_.name)");
        results2.put("Lower", Collections.singleton("Employee"));

        results.put("Upper", "SELECT UPPER(a1_.name) AS a2_ FROM Employee AS a1_ ORDER BY UPPER(a1_.name)");
        results2.put("Upper", Collections.singleton("Employee"));
    }

    protected DatabaseSchema getSchema() {
        return new DatabaseSchema(model, Collections.EMPTY_LIST, false);
    }
    public String getRegisterOffset1() {
        return "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ ORDER BY a1_.id";
    }
    public String getRegisterOffset2() {
        return "SELECT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE ";
    }
    public String getRegisterOffset3() {
        return "Employee AS a1_";
    }
    public String getRegisterOffset4() {
        return "WHERE";
    }
}
