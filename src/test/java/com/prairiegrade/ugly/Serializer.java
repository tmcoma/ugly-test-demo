package com.prairiegrade.ugly;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prairiegrade.ugly.entity.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dumps all the {@link Account} entries from the database
 * to the "data" directory.  You might want to use something
 * like this if you need to create some test data to work with,
 * e.g. dump what you need, then massage the data by hand to 
 * create your incoming test cases.
 */
public class Serializer {
    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(Constants.PERSISTENCE_UNIT);
        EntityManager em = emf.createEntityManager();

        // create a place to store all this test data
        Path dir = Paths.get("").resolve("data");
        Files.createDirectories(dir);
        GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.create();
        try {
            for (Account account : em.createQuery("from Account", Account.class).getResultList()){
                var filename = String.format("%s.%s.json", account.getClass().getSimpleName(), account.getId());
                Path file = dir.resolve(filename);
                if(Files.exists(file)){
                    Files.delete(file);
                }
                Files.createFile(file);
                try(Writer writer = Files.newBufferedWriter(file)){
                    gson.toJson(account, writer);
                    logger.debug("Wrote {}#{} to {}", account.getClass(), account.getId(), file);
                }
            }
            logger.debug("Done inserting, cleaning up...");
        } finally {
            em.close();
            emf.close();
        }
    }
}
