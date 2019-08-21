package com.zendesk.module;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.zendesk.dao.TodoDao;
import com.zendesk.dao.TodoDaoImpl;
import com.zendesk.model.Todo;
import com.zendesk.routes.Router;
import com.zendesk.routes.TodoRouter;
import com.zendesk.service.TodoService;
import com.zendesk.service.TodoServiceImpl;
import io.jsondb.JsonDBTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TodoModule extends AbstractModule {

    private static final Logger log
            = LoggerFactory.getLogger(TodoModule.class);

    @Override
    protected void configure() {
        Properties prop = new Properties();
        try (
                InputStream is = getClass().getClassLoader().
                        getResourceAsStream("config.properties")) {
            prop.load(is);
            Names.bindProperties(binder(), prop);
        } catch (IOException ioe) {
            log.error("Exception while trying to load config.properties", ioe);
        }

        String dbFilesLocation = prop.getProperty("db.files.location");
        String baseScanPackage = prop.getProperty("base.scan.package");

        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);
        if (!jsonDBTemplate.collectionExists(Todo.class))
            jsonDBTemplate.createCollection(Todo.class);

        bind(JsonDBTemplate.class).toInstance(jsonDBTemplate);
        bind(new TypeLiteral<TodoDao<Todo>>() {
        }).to(TodoDaoImpl.class);
        bind(TodoService.class).to(TodoServiceImpl.class);
        bind(Router.class).annotatedWith(Names.named("TodoRouter")).to(TodoRouter.class);
    }
}
