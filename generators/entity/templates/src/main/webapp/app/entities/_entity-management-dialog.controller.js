'use strict';

angular.module('<%=angularAppName%>').controller('<%= entityClass %>ManagementDialogController',
    ['$scope', '$stateParams', '$uibModalInstance'<% if (fieldsContainOwnerOneToOne) { %>, '$q'<% } %><% if (fieldsContainBlob) { %>, 'DataUtils'<% } %>, 'entity', '<%= entityClass %>'<% for (idx in differentTypes) { if (differentTypes[idx] != entityClass) {%>, '<%= differentTypes[idx] %>'<% } } %>,
        function($scope, $stateParams, $uibModalInstance<% if (fieldsContainOwnerOneToOne) { %>, $q<% } %><% if (fieldsContainBlob) { %>, DataUtils<% } %>, entity, <%= entityClass %><% for (idx in differentTypes) { if (differentTypes[idx] != entityClass) {%>, <%= differentTypes[idx] %><% } } %>) {

        $scope.<%= entityInstance %> = entity;<%
            var queries = [];
            for (idx in relationships) {
                var query;
                if (relationships[idx].relationshipType == 'one-to-one' && relationships[idx].ownerSide == true && relationships[idx].otherEntityName != 'user') {
                    query = '$scope.' + relationships[idx].relationshipFieldName.toLowerCase() + 's = ' + relationships[idx].otherEntityNameCapitalized + ".query({filter: '" + relationships[idx].otherEntityRelationshipName.toLowerCase() + "-is-null'});"
                + "\n        $q.all([$scope." + entityInstance + ".$promise, $scope." + relationships[idx].relationshipFieldName.toLowerCase() + "s.$promise]).then(function() {";
                    if (dto == "no"){
                        query += "\n            if (!$scope." + entityInstance + "." + relationships[idx].relationshipFieldName + " || !$scope." + entityInstance + "." + relationships[idx].relationshipFieldName + ".id) {"
                    } else {
                        query += "\n            if (!$scope." + entityInstance + "." + relationships[idx].relationshipFieldName + "Id) {"
                    }
                    query += "\n                return $q.reject();"
                + "\n            }"
                + "\n            return " + relationships[idx].otherEntityNameCapitalized + ".get({id : $scope." + entityInstance + "." + relationships[idx].relationshipFieldName + (dto == 'no' ? ".id" : "Id") + "}).$promise;"
                + "\n        }).then(function(" + relationships[idx].relationshipFieldName + ") {"
                + "\n            $scope." + relationships[idx].relationshipFieldName.toLowerCase() + "s.push(" + relationships[idx].relationshipFieldName + ");"
                + "\n        });";
                } else {
                    query = '$scope.' + relationships[idx].otherEntityNameCapitalized.toLowerCase() + 's = ' + relationships[idx].otherEntityNameCapitalized + '.query();';
                }
                if (!contains(queries, query)) {
                    queries.push(query);
                }
            } %><% for (idx in queries) { %>
        <%- queries[idx] %><% } %>
        $scope.load = function(id) {
            <%= entityClass %>.get({id : id}, function(result) {
                $scope.<%= entityInstance %> = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.$emit('<%=angularAppName%>:<%= entityInstance %>Update', result);
            $uibModalInstance.close(result);
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.<%= entityInstance %>.id != null) {
                <%= entityClass %>.update($scope.<%= entityInstance %>, onSaveSuccess, onSaveError);
            } else {
                <%= entityClass %>.save($scope.<%= entityInstance %>, onSaveSuccess, onSaveError);
            }
        };

        $scope.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
        <%_ if (fieldsContainBlob) { _%>

        $scope.openFile = DataUtils.openFile;

        $scope.byteSize = DataUtils.byteSize;
        <%_ } _%>
        <%_ for (idx in fields) {
            if (fields[idx].fieldType === 'byte[]' && fields[idx].fieldTypeBlobContent != 'text') { _%>

        $scope.set<%= fields[idx].fieldNameCapitalized %> = function ($file, <%= entityInstance %>) {
            <%_ if (fields[idx].fieldTypeBlobContent == 'image') { _%>
            if ($file && $file.$error == 'pattern') {
                return;
            }
            <%_ } _%>
            if ($file) {
                var fileReader = new FileReader();
                fileReader.readAsDataURL($file);
                fileReader.onload = function (e) {
                    var base64Data = e.target.result.substr(e.target.result.indexOf('base64,') + 'base64,'.length);
                    $scope.$apply(function() {
                        <%= entityInstance %>.<%= fields[idx].fieldName %> = base64Data;
                        <%= entityInstance %>.<%= fields[idx].fieldName %>ContentType = $file.type;
                    });
                };
            }
        };
        <%_ } else if (fields[idx].fieldType === 'LocalDate' || fields[idx].fieldType === 'ZonedDateTime') { _%>
        $scope.datePickerFor<%= fields[idx].fieldNameCapitalized %> = {};

        $scope.datePickerFor<%= fields[idx].fieldNameCapitalized %>.status = {
            opened: false
        };

        $scope.datePickerFor<%= fields[idx].fieldNameCapitalized %>Open = function($event) {
            $scope.datePickerFor<%= fields[idx].fieldNameCapitalized %>.status.opened = true;
        };
        <%_ } } _%>
}]);
