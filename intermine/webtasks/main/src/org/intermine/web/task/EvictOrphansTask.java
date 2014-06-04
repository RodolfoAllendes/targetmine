package org.intermine.web.task;

import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.ObjectStoreWriterFactory;
import org.intermine.metadata.ConstraintOp;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.model.userprofile.Tag;

public class EvictOrphansTask extends Task {

    private ObjectStoreWriter userprofile;
    private String alias;
    
    public void setObjectStoreAlias(String alias) {
        this.alias = alias;
    }
    
    private ObjectStoreWriter getUserProfile() throws BuildException {
        if (alias == null) {
            throw new BuildException("objectStoreWriter attribute is not set");
        }
        if (userprofile == null) {
            System .out.println("Connecting to db: " + alias);
            try {
                userprofile = ObjectStoreWriterFactory.getObjectStoreWriter(alias);
            } catch (ObjectStoreException e) {
                throw new BuildException("Could not connect to object store", e);
            }
        }
        return userprofile;
    }
    
    @Override
    public void execute() {
        int removed = 0;
        removed += removeOrphanTags();
        System .out.printf("Success: removed %d orphaned objects.\n", removed);
    }
    
    private int removeOrphanTags() {
        // Basically: 
        //    select tag.id
        //    from tag, (select array(select id from userprofile) as ids) as sq
        //    where tag.userprofileid IS NULL OR tag.userprofileid <> ALL (sq.ids)
        //    order by tag.id
        ObjectStoreWriter osw = getUserProfile();
        Query q = new Query();
        QueryClass qc = new QueryClass(Tag.class);
        q.setConstraint(new SimpleConstraint(new QueryField(qc, "userProfile"), ConstraintOp.IS_NULL));
        Set<Object> res;
        try {
            res = osw.executeSingleton(q);
        } catch (Exception e) {
            throw new BuildException("Could not retrieve any tags",e);
        }
        try {
            osw.beginTransaction();
            for (Object o: res) {
                osw.delete((Tag) o);
            }
            osw.commitTransaction();
        } catch (Exception e) {
            throw new BuildException("Error deleting tags", e);
        } finally {
          if (osw != null) {
              try {
                  if (osw.isInTransaction()) {
                      osw.abortTransaction();
                  }
              } catch (ObjectStoreException e) {
                  throw new BuildException("Could not even manage transactions here...", e);
              }
          }
        }
        return res.size();
    }
}

