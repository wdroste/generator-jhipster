package <%=packageName%>.service<% if (service == 'serviceImpl') { %>.impl<% } %>;
<%  var serviceClassName = service == 'serviceImpl' ? entityClass + 'ServiceImpl' : entityClass + 'Service';
    var viaService = false;
    var instanceType = (dto == 'mapstruct') ? entityClass + 'DTO' : entityClass;
    var instanceName = (dto == 'mapstruct') ? entityInstance + 'DTO' : entityInstance;
    var mapper = entityInstance  + 'Mapper';
    var dtoToEntity = mapper + '.'+ entityInstance +'DTOTo' + entityClass;
    var entityToDto = mapper + '.'+ entityInstance +'To' + entityClass + 'DTO';
    var entityToDtoReference = mapper + '::'+ entityInstance +'To' + entityClass + 'DTO';
    var repository = entityInstance  + 'Repository';
    var searchRepository = entityInstance  + 'SearchRepository';
    if (service == 'serviceImpl') { %>
import <%=packageName%>.service.<%= entityClass %>Service;<% } %>
import <%=packageName%>.domain.<%= entityClass %>;
import <%=packageName%>.repository.<%= entityClass %>Repository;<% if (searchEngine == 'elasticsearch') { %>
import <%=packageName%>.repository.search.<%= entityClass %>SearchRepository;<% } if (dto == 'mapstruct') { %>
import <%=packageName%>.web.rest.dto.<%= entityClass %>DTO;
import <%=packageName%>.web.rest.mapper.<%= entityClass %>Mapper;<% } %>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;<% if (pagination != 'no') { %>
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;<% } if (databaseType == 'sql') { %>
import org.springframework.transaction.annotation.Transactional;<% } %>
import org.springframework.stereotype.Service;

import javax.inject.Inject;<% if (dto == 'mapstruct') { %>
import java.util.LinkedList;<% } %>
import java.util.List;<% if (databaseType == 'cassandra') { %>
import java.util.UUID;<% } %><% if (searchEngine == 'elasticsearch' || dto == 'mapstruct' || fieldsContainNoOwnerOneToOne == true) { %>
import java.util.stream.Collectors;<% } %><% if (searchEngine == 'elasticsearch' || fieldsContainNoOwnerOneToOne == true) { %>
import java.util.stream.StreamSupport;<% } %><% if (searchEngine == 'elasticsearch') { %>

import static org.elasticsearch.index.query.QueryBuilders.*;<% } %>

/**
 * Service Implementation for managing <%= entityClass %>.
 */
@Service<% if (databaseType == 'sql') { %>
@Transactional<% } %>
public class <%= serviceClassName %> <% if (service == 'serviceImpl') { %>implements <%= entityClass %>Service<% } %>{

    private final Logger log = LoggerFactory.getLogger(<%= serviceClassName %>.class);
    <%- include('../../common/inject_template', {viaService: viaService}); -%>
    /**
     * Save a <%= entityInstance %>.
     * 
     * @param <%= instanceName %> the entity to save
     * @return the persisted entity
     */
    public <%= instanceType %> save(<%= instanceType %> <%= instanceName %>) {
        log.debug("Request to save <%= entityClass %> : {}", <%= instanceName %>);<%- include('../../common/save_template', {viaService: viaService}); -%>
        return result;
    }

    /**
     *  Get all the <%= entityInstancePlural %>.
     *  <% if (pagination != 'no') { %>
     *  @param pageable the pagination information<% } %>
     *  @return the list of entities
     */<% if (databaseType == 'sql') { %>
    @Transactional(readOnly = true) <% } %>
    public <% if (pagination != 'no') { %>Page<<%= entityClass %><% } else { %>List<<%= instanceType %><% } %>> findAll(<% if (pagination != 'no') { %>Pageable pageable<% } %>) {
        log.debug("Request to get all <%= entityClassPlural %>");<% if (pagination == 'no') { %>
        List<<%= instanceType %>> result = <%= entityInstance %>Repository.<% if (fieldsContainOwnerManyToMany == true) { %>findAllWithEagerRelationships<% } else { %>findAll<% } %>()<% if (dto == 'mapstruct') { %>.stream()
            .map(<%= entityToDtoReference %>)
            .collect(Collectors.toCollection(LinkedList::new))<% } %>;<% } else { %>
        Page<<%= entityClass %>> result = <%= entityInstance %>Repository.findAll(pageable); <% } %>
        return result;
    }
<%- include('../../common/get_filtered_template'); -%>
    /**
     *  Get one <%= entityInstance %> by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */<% if (databaseType == 'sql') { %>
    @Transactional(readOnly = true) <% } %>
    public <%= instanceType %> findOne(<%= pkType %> id) {
        log.debug("Request to get <%= entityClass %> : {}", id);<%- include('../../common/get_template', {viaService: viaService}); -%>
        return <%= instanceName %>;
    }

    /**
     *  Delete the  <%= entityInstance %> by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(<%= pkType %> id) {
        log.debug("Request to delete <%= entityClass %> : {}", id);<%- include('../../common/delete_template', {viaService: viaService}); -%>
    }<% if (searchEngine == 'elasticsearch') { %>

    /**
     * Search for the <%= entityInstance %> corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */<% if (databaseType == 'sql') { %>
    @Transactional(readOnly = true) <% } %>
    public List<<%= instanceType %>> search(String query) {
        <%- include('../../common/search_template', {viaService: viaService}); -%>
    }<% } %>
}
