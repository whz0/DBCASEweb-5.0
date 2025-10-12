var normalize = (function () {
    var from = "ÃÀÁÄÂÈÉËÊÌÍÏÎÒÓÖÔÙÚÜÛãàáäâèéëêìíïîòóöôùúüûÑñÇç",
        to = "AAAAAEEEEIIIIOOOOUUUUaaaaaeeeeiiiioooouuuunncc",
        mapping = {};

    for (var i = 0, j = from.length; i < j; i++)
        mapping[from.charAt(i)] = to.charAt(i);

    return function (str) {
        if (str == undefined)
            return '';
        var ret = [];
        for (var i = 0, j = str.length; i < j; i++) {
            var c = str.charAt(i);
            if (mapping.hasOwnProperty(str.charAt(i)))
                ret.push(mapping[c]);
            else
                ret.push(c);
        }
        return ret.join('');
    }

})();

$(document).ready(function () {

    // Al pintar el modal
    $('#modalAddItem').on('shown.bs.modal', function () {
        switch ($("#tipoAdd").val()) {
            case "addEntity":
            case "addRelation":
            case "addDomain":
            case "addAttribute":
            case "addSuperEntity":
                $("#recipient-name").focus();
                break;
            case "addEntitytoRelation":
                $("#roleName").focus();
            case "addConstraints":
                $("#list0").focus();
                break;
        }
    });

    // Al esconder el modal
    $('#modalAddItem').on('hide.bs.modal', function () {
        switch ($("#tipoAdd").val()) {
            case "addEntity":
                break;
            case "addRelation":
            case "addDomain":
            case "addAttribute":
                $("#recipient-name").unbind("focus");
                break;
            case "addEntitytoRelation":
                $("#roleName").unbind("focus");
            case "addConstraints":
                $("#list0").unbind("focus");
                break;
        }

    });

    $('#btnHit').on('click', function () {
        $("#serverInput").val("localhost");
        $("#portInput").val("3306");
        $("#databaseInput").val("test");
        $("#usernameInput").val("root");
        $("#executeScriptSQL").addClass("disabled");
    });

    $('#serverInput, #portInput, #databaseInput, #usernameInput, #passInput').on('change', function () {
        $("#executeScriptSQL").addClass("disabled");
    });

    $('#executeScriptSQL').on('click', function () {
        console.log("/executeQueries");
        var myObj = {};
        // tomamos la informacion de la conexión ingresada en el modal
        myObj["data1"] = $("#serverInput").val();
        myObj["data2"] = $("#portInput").val();
        myObj["data3"] = $("#databaseInput").val();
        myObj["data4"] = $("#usernameInput").val();
        myObj["data5"] = $("#passInput").val();
        myObj["data6"] = $("#selectLenguage").val();
        myObj["data7"] = $("#resultSPhysicalSchema").html();
        var json = JSON.stringify(myObj);

        $.ajax({
            type: 'POST',
            url: '/executeQueries',
            data: json,
            contentType: "application/json",
            success: function (data) {
                var dataResponse = JSON.parse(data);
                if (dataResponse.data2 == true) {
                    alert(dataResponse.data1);
                    $("#executeScriptSQL").removeClass("disabled");
                } else {
                    alert(dataResponse.data1);
                    $("#executeScriptSQL").addClass("disabled");
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {

            }

        });
    });

    $('#testConnection').on('click', function () {
        console.log("//checkConnection");
        var myObj = {};

        // tomamos la informacion de la conexión ingresada en el modal
        myObj["data1"] = $("#serverInput").val();
        myObj["data2"] = $("#portInput").val();
        myObj["data3"] = $("#databaseInput").val();
        myObj["data4"] = $("#usernameInput").val();
        myObj["data5"] = $("#passInput").val();
        myObj["data6"] = $("#selectLenguage").val();
        var json = JSON.stringify(myObj);

        $.ajax({
            type: 'POST',
            url: '/checkConnection',
            data: json,
            contentType: "application/json",
            success: function (data) {
                var dataResponse = JSON.parse(data);
                if (dataResponse.data2 == true) {
                    alert(dataResponse.data1);
                    $("#executeScriptSQL").removeClass("disabled");
                } else {
                    alert(dataResponse.data1);
                    $("#executeScriptSQL").addClass("disabled");
                }


            },
            error: function (xhr, ajaxOptions, thrownError) {
                console.log(xhr.status);
                console.log(xhr.responseText);
                console.log(thrownError);
            }

        });
    });

    $('#executeScriptSQL').on('click', function () {

    });

    $('#btnTest').on('click', function () {
        //var url = "<c:url value="/generateData"/>";
        console.log("/generateData");
        var f = 2;
        var myObj = {};

        var auxNodesTotal = nodes.get();
        //var nodesSuper = nodes_super.get();
        var resultNodes = [];
        var agregation = "";
        auxNodesTotal.forEach(function (item, index) {
            var auxItem = item;
            if (item.isWeak || item.isWeak == "active")
                auxItem.isWeak = true;
            else
                auxItem.isWeak = false;
            if (item.IsSuperEntity) {
                agregation = item.label;
                auxItem = {
                    heightConstraint: 25,
                    id: item.id,
                    isWeak: false,
                    label: item.label,
                    physics: false,
                    scale: 10,
                    shape: "box",
                    superEntity: -1,
                    isSuperEntity: true,
                    widthConstraint: '',
                    maximum: 200,
                    minimum: 100,
                    x: item.x,
                    y: item.y,
                };
                resultNodes.push(auxItem);

            } else if (item.superEntity < 0) {
                resultNodes.push(auxItem);
            }

            switch (item.shape) {
                case "diamond":
                    item.color = '#FF3F20';
                    break;
                case "triangleDown":
                    item.color = '#FF952A';
                    break;
                case "ellipse":
                    item.color = '#22bdb1';
                    break;
            }

        });

        // tomamos la informacion de las entidades externas a la agregación
        var traduct = {};
        for (var i = 0; i < resultNodes.length; i++) {
            var tempLabel = resultNodes[i].label;
            resultNodes[i].label = normalize(resultNodes[i].label);
            var nameLabel = resultNodes[i].label;
            traduct[nameLabel] = tempLabel;
        }

        var edgesData = edges.get();

        for (var i = 0; i < edgesData.length; i++) {
            if (edgesData[i].participation) {

            }

            var tempLabel = edgesData[i].label;

            if (edgesData[i].label) {
                edgesData[i].label = normalize(edgesData[i].label);
            }
            var nameLabel = edgesData[i].label;
            traduct[nameLabel] = tempLabel;

            var tempName = edgesData[i].name;
            if (edgesData[i].name) {
                edgesData[i].name = normalize(edgesData[i].name);
            }
            var nameName = edgesData[i].name;
            traduct[nameName] = tempName;
        }
        var nodesSuper = nodes_super.get();
        for (var i = 0; i < nodesSuper.length; i++) {
            var tempLabel = nodesSuper[i].label;
            nodesSuper[i].label = normalize(nodesSuper[i].label);
            var nameLabel = nodesSuper[i].label;
            traduct[nameLabel] = tempLabel;
            switch (nodesSuper[i].shape) {
                case "diamond":
                    nodesSuper[i].color = '#FF3F20';
                    break;
                case "triangleDown":
                    nodesSuper[i].color = '#FF952A';
                    break;
                case "ellipse":
                    nodesSuper[i].color = '#22bdb1';
                    break;
            }

        }

        var edgesSuperData = edges_super.get();
        console.log("edgesSuperData: " + edgesSuperData.length);
        for (var i = 0; i < edgesSuperData.length; i++) {
            console.log("edgesSuperData entra");
            if (edgesSuperData[i].participation) {

            }
            var tempLabel = edgesSuperData[i].label;
            if (edgesSuperData[i].label) {
                edgesSuperData[i].label = normalize(edgesSuperData[i].label);
            }
            var nameLabel = edgesSuperData[i].label;
            traduct[nameLabel] = tempLabel;

            var tempName = edgesSuperData[i].name;
            if (edgesSuperData[i].name) {
                edgesSuperData[i].name = normalize(edgesSuperData[i].name);
            }
            var nameName = edgesSuperData[i].name;
            traduct[nameName] = tempName;
        }
        console.log("resultNodes: " + resultNodes.length);
        console.log("edgesData: " + edgesData.length);
        myObj["data1"] = JSON.stringify(resultNodes);
        myObj["data2"] = JSON.stringify(edgesData);

        //tomamos la data de la entidad relacion de alto nivel
        myObj["data3"] = JSON.stringify(nodesSuper);
        myObj["data4"] = JSON.stringify(edgesSuperData);
        var json = JSON.stringify(myObj);
        //console.log(json);
        $.ajax({
            type: 'POST',
            url: '/generateData',
            data: json,
            contentType: "application/json",
            success: function (data) {
                for (const prop in traduct) {
                    //si existe un * en el regex le ponemos el caracter de escape
                    var auxConst = prop;
                    if (auxConst !== undefined && auxConst.indexOf("*") == auxConst.length - 1)
                        auxConst = auxConst.substr(0, auxConst.length - 1) + "\\*";
                    //auxConst = "test\\*";

                    var re = new RegExp(auxConst, "g");
                    data = data.replace(re, traduct[prop]);
                }

                data = data.replace(/\*\*/g, "*");
                data = data.replace(/agregacion/g, agregation);
                //data = data.replace(/\_/g, " ");
                //console.log(data);
                $("#testResult").html(data);
            },
            error: function (xhr, ajaxOptions, thrownError) {
                console.log("error generate data:" + edgesData);
            }

        });
    });

    $('#btnTestScriptSQL').on('click', function () {
        //var url = "<c:url value="/generateData"/>";
        console.log("/btnTestScriptSQL");
        var f = 2;
        var myObj = {};

        var auxNodesTotal = nodes.get();
        var resultNodes = [];
        var agregation = "";
        auxNodesTotal.forEach(function (item, index) {
            var auxItem = item;
            if (item.isWeak || item.isWeak == "active")
                auxItem.isWeak = true;
            else
                auxItem.isWeak = false;
            if (item.IsSuperEntity) {
                agregation = item.label;
                auxItem = {
                    heightConstraint: 25,
                    id: item.id,
                    isWeak: false,
                    label: item.label,
                    physics: false,
                    scale: 10,
                    shape: "box",
                    superEntity: -1,
                    widthConstraint: '',
                    maximum: 200,
                    minimum: 100,
                    x: item.x,
                    y: item.y,
                };

                resultNodes.push(auxItem);
            } else if (item.superEntity < 0) {
                resultNodes.push(auxItem);
            }

            switch (item.shape) {
                case "diamond":
                    item.color = '#FF3F20';
                    break;
                case "triangleDown":
                    item.color = '#FF952A';
                    break;
                case "ellipse":
                    item.color = '#22bdb1';
                    break;
            }
        });

        var traduct = {};
        for (var i = 0; i < resultNodes.length; i++) {
            var tempLabel = resultNodes[i].label;
            resultNodes[i].label = normalize(resultNodes[i].label);
            var nameLabel = resultNodes[i].label;
            traduct[nameLabel] = tempLabel;
        }
        var edgesData = edges.get();
        for (var i = 0; i < edgesData.length; i++) {
            if (edgesData[i].participation) {
                edgesData[i].labelFrom = edgesData[3].participationFrom;
                edgesData[i].labelTo = edgesData[3].participationTo;
            }
            var tempLabel = edgesData[i].label;
            if (edgesData[i].label) {
                edgesData[i].label = normalize(edgesData[i].label);
            }
            var nameLabel = edgesData[i].label;
            traduct[nameLabel] = tempLabel;

            var tempName = edgesData[i].name;
            if (edgesData[i].name) {
                edgesData[i].name = normalize(edgesData[i].name);
            }
            var nameName = edgesData[i].name;
            traduct[nameName] = tempName;
        }

        var nodesSuper = nodes_super.get();
        for (var i = 0; i < nodesSuper.length; i++) {
            var tempLabel = nodesSuper[i].label;
            nodesSuper[i].label = normalize(nodesSuper[i].label);
            var nameLabel = nodesSuper[i].label;
            traduct[nameLabel] = tempLabel;
            switch (nodesSuper[i].shape) {
                case "diamond":
                    nodesSuper[i].color = '#FF3F20';
                    break;
                case "triangleDown":
                    nodesSuper[i].color = '#FF952A';
                    break;
                case "ellipse":
                    nodesSuper[i].color = '#22bdb1';
                    break;
            }
        }

        var edgesSuperData = edges_super.get();
        for (var i = 0; i < edgesSuperData.length; i++) {
            if (edgesSuperData[i].participation) {
                edgesSuperData[i].labelFrom = edgesSuperData[3].participationFrom;
                edgesSuperData[i].labelTo = edgesSuperData[3].participationTo;
            }
            var tempLabel = edgesSuperData[i].label;
            if (edgesData[i].label) {
                edgesSuperData[i].label = normalize(edgesSuperData[i].label);
            }
            var nameLabel = edgesSuperData[i].label;
            traduct[nameLabel] = tempLabel;

            var tempName = edgesSuperData[i].name;
            if (edgesData[i].name) {
                edgesSuperData[i].name = normalize(edgesSuperData[i].name);
            }
            var nameName = edgesSuperData[i].name;
            traduct[nameName] = tempName;
        }

        myObj["data1"] = JSON.stringify(resultNodes);
        myObj["data2"] = JSON.stringify(edgesData);
        myObj["data3"] = JSON.stringify(nodesSuper);
        myObj["data4"] = JSON.stringify(edgesSuperData);
        myObj["data5"] = $("#selectLenguage option:selected").text();
        myObj["data6"] = $("#selectLenguage option:selected").index();

        var json = JSON.stringify(myObj);
        $.ajax({
            type: 'POST',
            url: '/generateDataScriptSQL',
            data: json,
            contentType: "application/json",
            success: function (data) {
                for (const prop in traduct) {
                    //si existe un * en el regex le ponemos el caracter de escape
                    var auxConst = prop;
                    if (auxConst !== undefined && auxConst.indexOf("*") == auxConst.length - 1)
                        auxConst = auxConst.substr(0, auxConst.length - 1) + "\\*";
                    //auxConst = "test\\*";

                    var re = new RegExp(auxConst, "g");
                    data = data.replace(re, traduct[prop]);
                }

                data = data.replace(/\*/g, "");
                data = data.replace(/agregacion/g, agregation);
                $("#resultSPhysicalSchema").html(data);
            },
            error: function (xhr, ajaxOptions, thrownError) {
                console.log(xhr.status);
                console.log(xhr.responseText);
                console.log(thrownError);
            }

        });
    });

    $('#insertModal').on('click', function () {

        switch ($('#tipoAdd').val()) {
            case "addConstraints":
                addConstraints($('input[name=listText\\[\\]]').serializeArray(), $('#idSelected').val(), $('#typeAction').val());
                break;
            case "addEntity":
                addEntity($('#recipient-name').val(), $('#weak-entity').prop('checked'), $('#typeAction').val(), $('#idSelected').val(), $("#element").val(), $("#relationEntity").val());
                break;
            case "addRelation":
                addRelation($('#recipient-name').val(), $('#typeAction').val(), $('#idSelected').val());
                break;
            case "addAttribute":
                addAttribute($('#recipient-name').val(), $('#typeAction').val(), $('#idSelected').val(), $('#element').val(), $('#primaryKey').prop('checked'), $('#composite').prop('checked'), $('#notNull').prop('checked'), $('#unique').prop('checked'), $('#multivalued').prop('checked'), $('#domain').val(), $('#size').val());
                break;
            case "addEntitytoRelation":
                addEntitytoRelation($('#element').val(), $('#element_role').val(), $('[name=cardinality]:checked').val(), $('#roleName').val(), $('#minCardinality').val(), $('#maxCardinality').val(), $('#typeAction').val(), $('#idSelected').val(), $("#minMax").prop('checked'));
                break;
            case "addEntityParent":
                addEntityParent($('#element').val(), $('#typeAction').val(), $('#idSelected').val());
                break;
            case "removeEntitytoRelation":
                removeEntitytoRelation($('#element').val(), $('#typeAction').val(), $('#idSelected').val());
                break;
            case "addEntityChild":
                addEntityChild($('#element').val(), $('#typeAction').val(), $('#idSelected').val());
                break;
            case "addDomain":
                addTypeDomain($('#recipient-name').val(), $('#types').val(), $('#values_separated').val(), $('#typeAction').val());
                break;
            case "addTableUnique":
                addTableUnique(groupByArray($('#formInsert').serializeArray()), $('#idSelected').val(), $('#typeAction').val());
                break;
            case "removeChildEntity":
                removeEntitytoRelation($('#element').val(), $('#typeAction').val(), $('#idSelected').val());
                break;
            case "addSubAtribute":
                addSubAttribute($('#recipient-name').val(), $('#typeAction').val(), $('#idSelected').val(), $('#element').val(), $('#composite').prop('checked'), $('#notNull').prop('checked'), $('#unique').prop('checked'), $('#multivalued').prop('checked'), $('#domain').val(), $('#size').val());
                break;
            case "addSuperEntity":
                addSuperEntity(parseInt($('#idSelected').val()), $('#recipient-name').val(), $('#typeAction').val());
                break;
            case "addIsA":
                break;
            default:
        }
    });

    $('.insertarDatos').on('click', function () {
        var insert = ""
        var nodo_select = getNodeSelected();
        switch ($(this).attr("functionInsert")) {
            case "addConstraints":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAddConstraints').tmpl(dataType));
                editList();
                eventsAddConstraints();
                break;
            case "addEntity":
                nodo = getAllNodes(["box"]);
                var dataType = {
                    temp_node_select: nodo_select,
                    temp_ent_length: nodo.length,
                    temp_nodes: nodo
                };
                $('#formModal').html($('#templateAddEntity').tmpl(dataType));
                eventAddEventRecipient();
                eventAddEntity();
                break;
            case "addRelation":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAddRelation').tmpl(dataType));
                eventAddEventRecipient();
                break;
            case "addAtribute":
                nodo = getAllNodes(["box", "diamond"]);
                types = getAllTypesDomain();
                if (nodo_select != null) {
                    var dataType = {
                        temp_node_length: nodo.length,
                        temp_nodes: nodo,
                        temp_types: types,
                        temp_node_select: nodo_select,
                        temp_type_item: getTypeItem(nodo_select)
                    };
                } else {
                    var dataType = {
                        temp_node_length: nodo.length,
                        temp_nodes: nodo,
                        temp_types: types,
                        temp_node_select: nodo_select
                    };
                }

                $('#formModal').html($('#templateAddAtribute').tmpl(dataType));
                eventAddEventRecipientAttribute();
                eventEventPrimaryKeyAttribute();
                break;
            case "addSubAtribute":
                nodo = getAllNodes(["box", "diamond"]);
                types = getAllTypesDomain();
                if (nodo_select != null) {
                    var dataType = {
                        temp_types: types,
                        temp_node_select: nodo_select
                    };
                } else {
                    var dataType = {
                        temp_types: types,
                        temp_node_select: nodo_select
                    };
                }

                $('#formModal').html($('#templateAddSubAtribute').tmpl(dataType));
                eventAddEventRecipient();
                eventSubAttribute();
                break;
            case "addEntitytoRelation":
                var nodoRoles;
                var action = $('#typeAction').val();
                var nodeAux = nodes.get(nodo_select);

                var childs = allUniqueEntityOfRelation(nodeAux.id);

                if (action == "create") {
                    if (nodeAux.superEntity < 0)
                        nodo = getAllNodes(["box"]);
                    else nodo = getEntitiesNotInSuperEntity(nodeAux.superEntity, null);
                } else {
                    if (nodeAux.superEntity < 0)
                        nodo = childs;
                    else nodo = getEntitiesNotInSuperEntity(nodeAux.superEntity, nodeAux.id);
                }
                var selection = -1;
                var selectionRoles = -1;
                for (var i = 0; i < nodo.length; i++) {
                    if (!inArray1(nodo[i].id, childs)) {
                        selection = nodo[i].id;
                    }

                }
                if (nodo.length > 0) {
                    selection = nodo[0].id;
                    nodoRoles = allEntitysToRelation2(nodo_select, "box", nodo[0].id);
                    if (nodoRoles.length > 0)
                        selectionRoles = nodoRoles[0].id;
                }

                var min = "", max = "", asoc = "", cardinalidad = "";

                if (nodoRoles.length > 0 && nodoRoles[0].asoc !== undefined) {

                    asoc = nodoRoles[0].asoc.charAt(0),
                        cardinalidad = nodoRoles[0].asoc.charAt(2)
                }

                var esEditYRol = false;
                var cardinalidad1 = false;
                var minmax = false;
                var hayRol = false;
                var rol = "";
                if (nodoRoles.length > 0) {
                    if (nodoRoles[0].role.indexOf("(") != -1) {
                        min = nodoRoles[0].role.slice(2, nodoRoles[0].role.indexOf(",")).trim();
                        max = nodoRoles[0].role.slice(nodoRoles[0].role.indexOf(",") + 1, nodoRoles[0].role.indexOf(")") - 1).trim();
                        rol = nodoRoles[0].role.slice(nodoRoles[0].role.indexOf(")") + 1, nodoRoles[0].role.length).trim();
                        if (action == 'edit') {
                            minmax = true;
                        }
                    } else {
                        rol = nodoRoles[0].role;
                    }
                }

                if (rol.length > 0 && action == 'edit') hayRol = true

                if (action == 'edit' && nodoRoles.length == 0) {
                    esEditYRol = true;
                }
                if (action == 'edit' && cardinalidad == 1) {
                    cardinalidad1 = true;
                }

                var idSuperEntity = nodes.get(nodo_select).superEntity;
                var auxNodoLength = nodo.length;


                var dataType = {
                    temp_nodeRoles_length: nodoRoles.length,
                    temp_node_length: nodo.length,
                    temp_aux_node_length: auxNodoLength,
                    temp_nodes: nodo,
                    temp_node_roles: nodoRoles,
                    temp_node_select: nodo_select,
                    temp_id_superEntity: idSuperEntity,
                    temp_option_selection: selection,
                    temp_option_selection_roles: selectionRoles,
                    temp_min: min,
                    temp_max: max,
                    temp_asoc: asoc,
                    temp_action: action,
                    temp_editCorrect: esEditYRol,
                    temp_cardinalidad1: cardinalidad1,
                    temp_minMax: minmax,
                    temp_rol: rol,
                    temp_hayRol: hayRol
                };

                $('#formModal').html($('#templateAddEntitytoRelation').tmpl(dataType));
                eventsEntityToRelation();

                $(document).on('change', '#element', function () {
                    var entityId = $(this).val();

                    var nodoRolesAux = allEntitysToRelation2(nodo_select, "box", entityId);
                    var elem_role = document.getElementById('element_role');
                    elem_role.innerHTML = '';
                    nodoRolesAux.forEach(function (item) {

                        cardinalidad = nodoRolesAux[0].asoc.charAt(2);
                        var max1 = document.getElementById('max1');
                        var maxN = document.getElementById('maxN');
                        if (cardinalidad == 'N') {
                            max1.checked = false;
                            maxN.checked = true;
                        } else {
                            max1.checked = true;
                            maxN.checked = false;
                        }
                        var min = nodoRolesAux[0].asoc.charAt(0);

                        var parcial = document.getElementById('parcial');
                        var total = document.getElementById('total');
                        if (min == '0') {
                            parcial.checked = false;
                            total.checked = true;
                        } else {
                            parcial.checked = true;
                            total.checked = false;
                        }

                        var op = document.createElement('option');
                        op.value = item.id;
                        op.textContent = item.label + " - " + item.asoc + " - " + item.role;
                        elem_role.appendChild(op);

                    });

                });
                break;
            case "addEntityParent":
                nodo = getAllNodes(["box"]);
                var valueExistParent = existParent(nodo_select);
                var numIdParent = -1;
                var childs = getChildData(nodo_select);
                nodo = nodo.filter(function (elem) {
                    return !inArray(elem.id, childs);
                });
                var dataType = {
                    temp_exist_parent: valueExistParent,
                    temp_node_length: nodo.length,
                    temp_nodes: nodo,
                    temp_node_select: nodo_select
                };

                $('#formModal').html($('#templateAddEntityParent').tmpl(dataType));
                break;
            case "removeChildEntity":
                nodo = getChildData(nodo_select);
                var dataType = {
                    temp_node_length: nodo.length,
                    temp_nodes: nodo,
                    temp_node_select: nodo_select
                };

                $('#formModal').html($('#templateRemoveChildEntity').tmpl(dataType));
                break;
            case "addEntityChild":
                nodo = getAllNodes(["box"]);
                var valueExistParent = existParent(nodo_select);
                var numIdParent = -1;
                var childs = getChildData(nodo_select);
                nodo = nodo.filter(function (elem) {
                    return !inArray(elem.id, childs);
                });
                if (valueExistParent)
                    numIdParent = getParentId(nodo_select);
                var dataType = {
                    temp_exist_parent: valueExistParent,
                    temp_numIdParent: numIdParent,
                    temp_node_length: nodo.length,
                    temp_nodes: nodo,
                    temp_node_select: nodo_select
                };

                $('#formModal').html($('#templateAddEntityChild').tmpl(dataType));
                break;
            case "removeEntitytoRelation":
                nodo = allEntitysToRelation2(nodo_select, "box", null);
                var dataType = {
                    temp_node_length: nodo.length,
                    temp_nodes: nodo,
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateRemoveEntitytoRelation').tmpl(dataType));
                eventsRemoveEntityToRelation();
                break;
            case "createDomain":
                types = getAllTypesDomain();
                var dataType = {
                    temp_types: types,
                    temp_node_select: nodo_select
                };

                $('#formModal').html($('#templateCreateDomain').tmpl(dataType));
                eventAddEventRecipient();
                break;
            case "addUniqueKey":
                nodo = allAttributeOfEntity(nodo_select);
                var dataType = {
                    temp_node_length: nodo.length,
                    temp_nodes: nodo,
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAddUniqueKey').tmpl(dataType));
                $('.select-multiple').select2();
                editList();
                break;
            case "downloadFile":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateDownloadFile').tmpl(dataType));
                break;
            case "loadFile":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAddLoadFile').tmpl(dataType));
                break;
            case "addTextAbout":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAbout').tmpl(dataType));
                break;
            case "deleteSuperEntity":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateSuperEntity').tmpl(dataType));
                eventAddSuperEntity();
                break;
            case "addTextAgregation":
                var dataType = {
                    temp_node_select: nodo_select
                };
                $('#formModal').html($('#templateAddSuperEntity').tmpl(dataType));
                eventAddEventRecipient();
                break;
            case "removeParentIsA":
                if (existParent(nodo_select))
                    removeParentIsA(nodo_select);
                break;
            default:
        }
        setNodeSelected(null);
    });

    $('#undoLastAction').on('click', function () {
        undoLastAction();
    });

    $(document).on('click', '#redoLastAction', function () {
        redoLastAction();
    });

});