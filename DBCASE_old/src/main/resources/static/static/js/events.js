function groupByArray(xs) {
    var resultado = xs.reduce(function (rv, x) {
        (rv[x.name] = rv[x.name] || []).push(x.value);
        return rv;
    }, {});

    return Object.values(resultado);
}

function inArray(needle, haystack) {
    var length = haystack.length;
    for (var i = 0; i < length; i++) {
        if (haystack[i].idChild == needle) return true;
    }
    return false;
}

function inArray1(needle, haystack) {
    var length = haystack.length;
    for (var i = 0; i < length; i++) {
        if (haystack[i].id == needle) return true;
    }
    return false;
}

function inArray2(needle, haystack) {
    var length = haystack.length;
    for (var i = 0; i < length; i++) {
        if (haystack[i] == needle) return true;
    }
    return false;
}

function editList() {

    $("#addListUnique").click(function () {
        var nodo = allAttributeOfEntity(parseInt($("#idSelected").val()));
        var nextValue = parseInt($("#totalInputs").val()) + 1;
        var dataType = {
            temp_nodes: nodo,
            temp_unique: nextValue,
            temp_value: ""
        };
        $("#totalInputs").val(nextValue);
        $("#inputList").append($('#templateSelectTableUnique').tmpl(dataType));
        $('.select-multiple').select2();

    });

    $(document).on('click', '.removeList', function () {
        $("#uniqueField" + $(this).val()).remove();
    });

    $("#addListSelectConst").click(function () {
        var nextValue = parseInt($("#totalInputs").val()) + 1;
        var dataType = {
            temp_unique: nextValue,
            temp_value: ""
        };
        $("#totalInputs").val(nextValue);
        $("#inputList").append($('#templateSelectAddConstraints').tmpl(dataType));
    });
}

function eventAddSuperEntity() {
    $("#deleteSuper1").click(function () {
        deleteSuperEntity($("#idSelected").val());
        $('#modalAddItem').modal('hide');
    });
    $("#deleteSuper2").click(function () {
        deleteSuperEntityAndEelements($("#idSelected").val());
        $('#modalAddItem').modal('hide');
    });
}

function eventsEntityToRelation() {

    $("#element,#element_role, #max1,#parcial, #total, #maxN").change(function () {
        var idF = $("#element").val();
        var idT = $("#idSelected").val();

        var idEdge = existEdge(idF, idT);
        var roleName = $("#roleName").val();
        if (idEdge) {
            if ($("#roleName").val() == "" && $("#typeAction").val() == 'create') {

                $("#insertModal").prop("disabled", true);
                if ($("#textWarning").length == 0) {
                }

            } else {
                $("#insertModal").prop("disabled", false);
            }
        } else {
            if (edges.get(idEdge).label != '') $("#insertModal").prop("disabled", false);
            else $("#insertModal").prop("disabled", true);
            $('#roleName').val('');
            if ($("#textWarning").length > 0) {
                $('#textWarning').remove();
            }

        }
    });
    $("#maxCardinality").blur(function () {
        if ($("#minCardinality").val() != "" && $("#maxCardinality").val() == "") {
            $("#maxCardinality").val('N');
        }
    });
    $("#minCardinality").blur(function () {
        if ($("#minCardinality").val() == "" && $("#maxCardinality").val() != "") {
            if ($("#total").prop('checked')) {
                $("#minCardinality").val('1');
            }
        }
    });
    $("#minMax").click(function () {
        if ($("#minMax").prop('checked')) {
            $("#minCardinality").prop("disabled", false);
            $("#maxCardinality").prop("disabled", false);
            if ($("#parcial").prop('checked')) {
                $("#minCardinality").val(0);
                $("#maxCardinality").val('N');
                $("#minCardinality").prop("disabled", true);
            }
        } else {
            $("#minCardinality").val("");
            $("#maxCardinality").val("");
            $("#minCardinality").prop("disabled", true);
            $("#maxCardinality").prop("disabled", true);
        }
    });
    $("#parcial").click(function () {
        if ($("#parcial").prop('checked')) {
            if ($("#minMax").prop('checked')) {
                $("#minCardinality").val(0);
                $("#maxCardinality").val('N');
                $("#minCardinality").prop("disabled", true);
            } else {
                $("#minCardinality").val("");
                $("#maxCardinality").val("");
            }
        }
    });
    $("#total").click(function () {
        if ($("#minCardinality").val() != " ") {
            $("#minMax").prop("checked", true);
            $("#minCardinality").prop("disabled", false);
            $("#maxCardinality").prop("disabled", false);
            $("#minCardinality").val(1);
            $("#maxCardinality").val('N');
        }
    });
    $("#roleName").on('keydown', function (e) {
        try {
            var k;
            document.all ? k = e.keyCode : k = e.which;
            return ((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57) || k == 46 || k == 37 || k == 39);

        } catch (Exception) {
            return false;
        }
    });
    $("#roleName").on('input', function () {
        var idF = $("#element").val();
        var idT = $("#idSelected").val();
        var idEdge = existEdge(idF, idT);
        var roleName = $("#roleName").val();
        if (idEdge && edges.get(idEdge).label == '') {
            if ($("#roleName").val() == "" && $("#typeAction").val() == 'create') {
                $("#insertModal").prop("disabled", true);
                if ($("#textWarning").length == 0) {

                    $("#roleName").after("<span id='textWarning' class='text-warning'>" + $("#textNecesaryRol").text() + "</span>")
                }
            } else if ($("#roleName").val() != "") {
                if ($("#textWarning").length > 0)
                    $('#textWarning').remove();

                $("#insertModal").prop("disabled", false);

            }
        } else if (edges.get(idEdge).label == '') {
            //$('#roleName').val('');
            if ($("#textWarning").length > 0) {
                $('#textWarning').remove();
            }
            $("#insertModal").prop("disabled", false);
        }
    });
    $('#modalAddItem').on('shown.bs.modal', function (e) {
        var idF = $("#element").val();
        var idT = $("#idSelected").val();
        var idEdge = existEdge(idF, idT);
        if (idEdge) {
            if ($("#roleName").val() == "" && $("#typeAction").val() == 'create') {
                $("#roleName").val("ROL");
            }
        } else if ($("#typeAction").val() == 'create') {
            $("#roleName").val("");
        }

    });
    $('#modalAddItem').on('hidden.bs.modal', function () {
        $("#modalAddItem").unbind("shown.bs.modal");
    });
}

function eventsRemoveEntityToRelation() {

    $('#modalAddItem').on('hidden.bs.modal', function () {
        $("#modalAddItem").unbind("shown.bs.modal");
    });
}

/*
Verifica que el campo de restriccion no este vacio
*/
function eventsAddConstraints() {
    $("#list0").on("blur keyup", function () {
        var nameValue = $("#list0").val();
        if (nameValue == "") {
            $('#insertModal').prop('disabled', true);
        } else {
            $('#insertModal').prop('disabled', false);
        }
    });
}

function eventEventPrimaryKeyAttribute() {
    $("#primaryKey, #composite").change(function () {
        if (($("#primaryKey").prop('checked') && $("#composite").prop('checked')) ||
            ($("#primaryKey").prop('checked') && !$("#composite").prop('checked'))) {
            $("[for='notNull'],[for='unique'],[for='multivalued']").toggle(false);
        }
        if (!$("#primaryKey").prop('checked') && $("#composite").prop('checked')) {
            $("[for='notNull'],[for='unique']").toggle(false);
            $("[for='multivalued']").toggle(true);
        }
        if (!$("#primaryKey").prop('checked') && !$("#composite").prop('checked')) {
            $("[for='notNull'],[for='unique'],[for='multivalued']").toggle(true);
        }
    });
}

function eventAddEntity() {

    $("#weak-entity").change(function () {
        if ($('#weak-entity').prop('checked')) {
            if ($('#ent_length').val() == 0 || ($('#ent_length').val() == 1 && $('#typeAction').val() == "edit")) {
                alert($('#textCreateStrongEntity').text());
                $('#weak-entity').prop('checked', false);
            } else {
                $("#tempWeakEntity").slideDown("slow");
                $('#insertModal').prop('disabled', true);

                $("#relationEntity").on("blur keyup", function () {
                    if ($("#relationEntity").val() != "" && $("#recipient-name").val() != "")
                        $('#insertModal').prop('disabled', false);
                    else
                        $('#insertModal').prop('disabled', true);
                });

            }
        } else {
            $("#tempWeakEntity").slideUp("slow");
            $("#relationEntity").unbind("blur keyup");//terminar el select
        }
    });
}

function eventSubAttribute() {
    $("#composite").change(function () {
        if ($("#composite").prop('checked')) {
            $("[for='notNull'],[for='unique']").toggle(false);
        } else {
            $("[for='notNull'],[for='unique']").toggle(true);
        }
    });
}

function eventAddEventRecipientAttribute() {
    $("#recipient-name").on("blur keyup", function () {
        var nameValue = $("#recipient-name").val();
        var tipoAdd = $("#tipoAdd").val();
        if (nameValue != "") {
            $('#insertModal').prop('disabled', false);
        } else {
            $('#insertModal').prop('disabled', true);
        }
    });
}

function eventAddEventRecipient() {
    $("#recipient-name").on("blur keyup", function () {
        var nameValue = $("#recipient-name").val();
        var tipoAdd = $("#tipoAdd").val();
        if (!existElementName(nameValue, tipoAdd)) {
            $('#insertModal').prop('disabled', false);
            $("#recipient-name").removeClass("is-invalid");
        } else {
            //$('#insertModal').prop('disabled', true);
            $("#recipient-name").addClass("is-invalid");
        }
    });

    $("#relationEntity").on("blur keyup", function () {
        var nameValue = $("#relationEntity").val();
        var tipoAdd = $("#tipoAdd").val();
        if (!existElementName(nameValue, tipoAdd)) {
            $('#insertModal').prop('disabled', false);
            $("#relationEntity").removeClass("is-invalid");
        } else {
            //$('#insertModal').prop('disabled', true);
            $("#relationEntity").addClass("is-invalid");
        }
    });
}


function updateTableElementsSuperEntity() {
    var text = "";
    var nodo = getAllNodesSuper(["box"]);
    text += '<p class="h6 text-' + $("#textTheme").text() + '">Entidades</p>';
    for (var i = 0; i < nodo.length; i++) {
        text += '<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/entidad-small.png" style="width: 25px;" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + nodo[i].label + '</span></p>';
        var listAtributes = allAttributeOfEntitySuper(nodo[i].id);
        for (var e = 0; e < listAtributes.length; e++) {
            text += '<p class="card-link small ml-2" href="#" aria-expanded="true"><img src="static/images/attribute-small.png" class="rounded" style="width: 25px;"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + ' : ' + listAtributes[e].type + '(' + listAtributes[e].size + ')</span></p>';
        }
    }
    text += '<p class="h6 mt-2 text-' + $("#textTheme").text() + '">Relaciones</p>';

    var nodo = getAllNodesSuper(["diamond", "triangleDown"]);
    for (var i = 0; i < nodo.length; i++) {
        text += '<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/diamond-small.png" style="width: 25px;" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + nodo[i].label + '</span></p>';
        var listAtributes = allEntitysToRelationSuper(nodo[i].id, "box");
        for (var e = 0; e < listAtributes.length; e++) {
            var asoc = "";
            if (listAtributes[e].asoc.length < 10)
                asoc = ": " + listAtributes[e].asoc;
            text += '<p class="card-link small ml-2" href="#" aria-expanded="true"><img src="static/images/entidad-small.png" style="width: 25px;" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + '' + asoc + '</span></p>';
        }
        var listAtributes = allAttributeOfEntitySuper(nodo[i].id);
        for (var e = 0; e < listAtributes.length; e++) {
            text += '<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/attribute-small.png" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + ' : ' + listAtributes[e].type + '(' + listAtributes[e].size + ')</span></p>';
        }
    }
    return text;
}

function restartPanel() {
    $("#frame1").attr("class", "changeSizeWidth col-md-6 pr-0 pl-0 h-100 border-top");
    $("#frame2").attr("class", "col-md-2 h-100 border-top border-left border-right");
    $("#frame3").attr("class", "col-md-4 changeSizeWidthData border-top");
    $("#frame5").attr("class", "col-md-12 pr-0 pl-0 h-50");
    $("#frame4").attr("class", "col-md-12 pr-0 pl-0 h-50 border-bottom");
    $("#frame1").show();
    $("#frame2").show();
    $("#frame3").show();
    $("#frame5").show();
    $("#frame4").show();
}

function updateTableElements() {
    $("#resultSPhysicalSchema").text("");//limpia el panel del esquema fisico
    $("#testResult").text("");//limpia el panel del esquema logico
    $('#accordion').html("");
    var nodo = getAllNodes(["box", "image"]);
    for (var i = 0; i < nodo.length; i++) {
        var dataType = {
            nameE: nodo[i].label,
            idE: nodo[i].id
        };
        $('#accordion').append($('#templateElementEntity').tmpl(dataType));

        if (nodo[i].shape == "image") {
            var htmlSuperEntity = updateTableElementsSuperEntity();
            $('#childs-attribute' + nodo[i].id).append(htmlSuperEntity);
//			$('#childs-attribute'+nodo[i].id).append('<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/attribute-small.png" class="rounded"><span class="pl-1 text-'+$("#textTheme").text()+'">pepito</span></p>');
        } else {
            $('#childs-attribute' + nodo[i].id).html("");
            var listAtributes = allAttributeOfEntity(nodo[i].id);
            for (var e = 0; e < listAtributes.length; e++) {
                $('#childs-attribute' + nodo[i].id).append('<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/attribute-small.png" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + ' : ' + listAtributes[e].type + '(' + listAtributes[e].size + ')</span></p>');
            }
        }

    }

    $('#accordion2').html("");
    var nodo = getAllNodes(["diamond", "triangleDown"]);
    for (var i = 0; i < nodo.length; i++) {
        var dataType = {
            nameE: nodo[i].label,
            idE: nodo[i].id,
            shape: nodo[i].shape
        };
        $('#accordion2').append($('#templateElementRelation').tmpl(dataType));
        $('#childs-attribute' + nodo[i].id).html("");
        var listAtributes = allEntitysToRelation(nodo[i].id, "box");
        for (var e = 0; e < listAtributes.length; e++) {
            var asoc = "";
            if (listAtributes[e].asoc.length < 10)
                asoc = ": " + listAtributes[e].asoc;
            $('#childs-attribute' + nodo[i].id).append('<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/entidad-small.png" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + '' + asoc + '</span></p>');
        }

        var listAtributes = allAttributeOfEntity(nodo[i].id);
        for (var e = 0; e < listAtributes.length; e++) {
            $('#childs-attribute' + nodo[i].id).append('<p class="card-link small ml-0" href="#" aria-expanded="true"><img src="static/images/attribute-small.png" class="rounded"><span class="pl-1 text-' + $("#textTheme").text() + '">' + listAtributes[e].label + ' : ' + listAtributes[e].type + '(' + listAtributes[e].size + ')</span></p>');
        }
    }
}

(function ($) {
    $(".vis-zoomExtends").hide();
    // sidebar lateral desplegar
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
    });

    $("#general-about").click(function () {
        $("[functioninsert='addTextAbout']").click();
        $("#formModalButton").hide();
    });

    $(document).keydown(function (e) {
        if (e.which == 46) {
            if ($('#insertModal').prop('disabled')) {
                if (getNodesSelectedCount() != 0 && getNodesSelectedCount() > 1) {
                    deleteNodeSelected();
                } else {
                    if (getNodesSelectedCount() == 1) {
                        deleteNodeSelected();
                    }
                }
            }
        }
        if (e.which == 13) {
            e.preventDefault();
            if (!$('#insertModal').prop('disabled')) {
                $("#insertModal").click();
            }
        }
    });

    // Añadir la funcion a ejecutar en cada modal
    $('.closeSide').on('click', function () {
        $('#titleModal').html(this.getAttribute("alt"));
        $('#tipoAdd').val(this.getAttribute("functionInsert"));
    });
    // Ejecutar directamente addIsA
    $('#addIsA').on('click', function () {
        addIsA();
    });

    // cambiar tamaño de diagramas
    $('.vis-zoomExtends').on('click', function () {
        $('.changeSizeWidth').toggleClass('col-md-6');
        $('.changeSizeWidth').toggleClass('col-md-10');
        $('.changeSizeWidthData').toggleClass('col-md-12');
        $('.changeSizeWidthData').toggleClass('col-md-4');
    });

    // cambiar distribución de la vista
    $('.change-aparience').on('click', function () {
        restartPanel();
        if (!$('.changeSizeWidth').hasClass('col-md-6'))
            $("#diagram .vis-zoomExtends").click();

        $('.change-aparience').removeClass("active");
        $(this).addClass("active");

        if ($("#frame4").hasClass("float-left")) {
            $("#frame1").show();
            $("#frame2").addClass("col-md-2");
            $("#frame2").removeClass("col-md-4");
            $("#frame2").addClass("border-left");
            $("#frame2").show();
            $("#frame3").removeClass("col-md-12");
            $("#frame3").addClass("col-md-4");
            $("#frame3").removeClass("col-md-8");
            $("#frame4").addClass("col-md-12");
            $("#frame4").removeClass("h-100");
            $("#frame4").addClass("h-50");
            $("#frame4").removeClass("border-right");
            $("#frame4").removeClass("col-md-6 float-left");
            $("#frame4").addClass("border-bottom");
            $("#frame5").addClass("col-md-12");
            $("#frame5").removeClass("col-md-6 float-left");
            $("#frame5").removeClass("h-100");
            $("#frame5").addClass("h-50");
            $("#frame5").removeClass("pl-2");
        }

        if ($("#frame1").hasClass("col-md-8")) {
            $("#frame2").show();
            $("#frame1").removeClass("col-md-8");
            $("#frame1").addClass("col-md-6");
            $("#frame3").removeClass("border-left");
            $("#frame5").show();
            $("#frame4").removeClass("h-100");
            $("#frame4").addClass("h-50");
            $("#frame4").addClass("border-bottom");
        }

        if ($("#frame1").hasClass("col-md-10")) {
            $(".vis-zoomExtends").click();
        }
        sessionStorage.setItem('layoutActive', $(this).attr("value"));
        switch ($(this).attr("value")) {
            case "0":
                $("#frame1").hide();
                $("#frame2").removeClass("col-md-2");
                $("#frame2").addClass("col-md-2");
                $("#frame3").removeClass("col-md-4");
                $("#frame3").addClass("col-md-10");
                $("#frame4").removeClass("col-md-12");
                $("#frame5").removeClass("col-md-12");
                $("#frame4").addClass("col-md-6 float-left");
                $("#frame5").addClass("col-md-6 float-left");
                $("#frame4").removeClass("h-50");
                $("#frame4").addClass("h-100");
                $("#frame5").removeClass("h-50");
                $("#frame5").addClass("h-100");
                $("#frame4").removeClass("border-bottom");
                $("#frame4").addClass("border-right");
                $("#frame2").removeClass("border-left");
                $("#frame5").addClass("pl-2");
                break;
            case "1":
                break;
            case "2":
                if ($('.changeSizeWidth').hasClass('col-md-6'))
                    $("#diagram .vis-zoomExtends").click();
                break;
            case "3":
                $("#frame2").hide();
                $("#frame1").addClass("col-md-8");
                $("#frame1").removeClass("col-md-6");
                $("#frame3").addClass("border-left");
                $("#frame5").hide();
                $("#frame4").addClass("h-100");
                $("#frame4").removeClass("h-50");
                $("#frame4").removeClass("border-bottom");
                break;
            case "4":
                $("#frame1").hide();
                $("#frame2").hide();
                $("#frame2").addClass("col-md-4");
                $("#frame3").removeClass("col-md-4");
                $("#frame3").addClass("col-md-12");
                $("#frame4").removeClass("col-md-12");
                $("#frame5").removeClass("col-md-12");
                $("#frame4").addClass("col-md-6 float-left");
                $("#frame5").addClass("col-md-6 float-left");
                $("#frame4").removeClass("h-50");
                $("#frame4").addClass("h-100");
                $("#frame5").removeClass("h-50");
                $("#frame5").addClass("h-100");
                $("#frame4").removeClass("border-bottom");
                $("#frame4").addClass("border-right");
                $("#frame5").addClass("pl-2");
                break;
            case "5":
                $("#frame2").hide();
                $("#frame1").addClass("col-md-8");
                $("#frame3").removeClass("col-md-6");
                $("#frame3").addClass("border-left");
                break;
        }
    });

    // abrir modal add Domain
    $('#openCreateDomain').on('click', function () {
        $("[functioninsert='createDomain']").click();
    });

    $('.insertarDatos').on('click', function () {
        // Limpiar el modal cuando se cierra, se deshabilita el boton
        $('#modalAddItem').on('hidden.bs.modal', function (event) {
            $('#insertModal').show();
            $('#insertModal').text($('#textInsert').text());
            $('#formModal').html("");
            $('#insertModal').prop('disabled', true);
            $("#formModalButton").show();
            $('#sidebar').removeClass('active');
            $("[for='notNull'],[for='unique'],[for='multivalued'],[for='composite']").show();
        });
    });

    $('.dropdown').on('show.bs.dropdown', function () {
        $("#sticky-top").removeClass("sticky-top");
    });

    $('.dropdown').on('hidden.bs.dropdown', function () {
        $("#sticky-top").addClass("sticky-top");
    });

    $("#general-new").on('click', function () {
        if (nodes.get().length > 0 || nodes_super.get().length > 0) {
            var salir = confirm($("#textCerrarArchivo").text());
            if (salir) {
                sessionStorage.setItem('codeSave', "");
                location.reload();
            }
        } else {
            sessionStorage.setItem('codeSave', "");
            location.reload();
        }
    });

    function simuleClick() {
        var event = new PointerEvent('pointerdown');
        document.getElementsByClassName("vis-zoomExtendsScreen")[1].dispatchEvent(event);
    }

    $('#roleName').on('keydown', function (event) {
        if (event.key === 'Delete') {
            // Permite la tecla Delete
            console.log('Delete key pressed');
        }
    });

    $("#general-print").on('click', function () {
        $.when(simuleClick()).then(function () {
            var c = document.getElementsByTagName("canvas")[1];
            var ctx = c.getContext("2d");
            var dataURL = ctx.canvas.toDataURL('image/png', 1.0);
            var doc = new jsPDF()
            doc.setFontSize(13)
            doc.text(10, 12, $('#nameText').text());
            doc.text(170, 12, "DBCASE Web");
            doc.addImage(dataURL, 'PNG', 15, 40, 180, 160);

            //saveAs(doc.output('blob'), $('#idText').text()+""+(new Date().getMilliseconds())+".pdf");
            saveAs(doc.output('blob'), document.getElementById("docs-title").value + ".pdf");
        });
    });

    $("#down_schema_physical").on('click', function () {
        if ($("#resultSPhysicalSchema").html().length > 8) {
            var str = $("#resultSPhysicalSchema").html();
            var res = str.replace(/<\/p>/g, "\r\n");
            res = res.replace(/<p class="h5 text-dark font-weight-bold">/g, "");
            res = res.replace(/<p class="text-dark">/g, "");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-warning">/g, "#");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-danger">/g, "#");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-light">/g, "#");
            res = res.replace(/<\/div>/g, "");
            res = res.replace(/<u>/g, "");
            res = res.replace(/<\/u>/g, "");
            res = res.replace(/<p>/g, "");
            res = res.replace(/<strong>/g, "");
            res = res.replace(/<\/strong>/g, "");
            res = res.replace(/\*/g, "");
            res = "#" + $("#textGeneratedBy").text() + "\r\n" + res;
            var blob = new Blob([res], {type: "text/plain;charset=utf-8"});
            //saveAs(blob, $('#idText').text()+""+(new Date().getMilliseconds())+".sql");
            saveAs(blob, document.getElementById("docs-title").value + ".sql");

        }
    });

    $("#down_schema_logic").on('click', function () {
        if ($("#testResult").html().length > 8) {
            var str = $("#testResult").html();
            var res = str.replace(/<\/p>/g, "\r\n");
            res = res.replace(/<p class="h5 text-dark font-weight-bold">/g, "");
            res = res.replace(/<p class="text-dark">/g, "");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-warning">/g, "#");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-danger">/g, "#");
            res = res.replace(/<div class="pl-1 pt-1 pr-1 alert alert-light">/g, "#");
            res = res.replace(/<\/div>/g, "");
            res = res.replace(/<u>/g, "");
            res = res.replace(/<\/u>/g, "");
            res = res.replace(/<p>/g, "");
            res = res.replace(/&gt;/g, ">");
            res = "#" + $("#textGeneratedBy").text() + "\r\n" + res;
            var blob = new Blob([res], {type: "text/plain;charset=utf-8"});
            //cambiar nombre documento
            saveAs(blob, document.getElementById("docs-title").value + ".txt");
        }
    });

    printDomains();

})(jQuery);