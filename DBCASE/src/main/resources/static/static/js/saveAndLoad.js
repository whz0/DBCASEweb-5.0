function exportNetwork(type) {
    var nodesB = [];
    nodes.forEach(function (nod) {
        nod.x = network.getPosition(nod.id).x;
        nod.y = network.getPosition(nod.id).y;
        nodesB.push(nod);
    });
    var edgesB = edges.get();
    var nodes_superB = [];
    nodes_super.forEach(function (nod) {
        nod.x = network_super.getPosition(nod.id).x;
        nod.y = network_super.getPosition(nod.id).y;
        nodes_superB.push(nod);
    });
    var edges_superB = edges_super.get();

    var all_nodes = {
        nodesA: nodesB,
        edgesA: edgesB,
        nodes_superA: nodes_superB,
        edges_superA: edges_superB
    };
    // pretty print node data
    var exportValue = JSON.stringify(all_nodes, undefined, 2);

    if (type != "session") {
        var blob = new Blob([exportValue], {type: "application/json"});
        saveAs(blob, document.getElementById("docs-title").value + "" + ".dbw");//var x = document.getElementById("myForm").elements.namedItem("docs-title").value obtener el valor del input
    } else {
        sessionStorage.setItem('codeSave', exportValue);
        sessionStorage.setItem('actionHistorySave', JSON.stringify(actionHistory));
        sessionStorage.setItem('undoneHistorySave', JSON.stringify(undoneHistory));


    }
}

// load scheme

function importNetwork(type, value = null) {
    nodes.clear();
    edges.clear();
    nodes_super.clear();
    edges_super.clear();
    if (type != "session") {
        var inputValue = value;
    } else {
        var inputValue = sessionStorage.getItem('codeSave');
        /*var aHistory = sessionStorage.getItem('actionHistorySave');
        var uHistory = sessionStorage.getItem('undoneHistorySave');*/

        actionHistory = JSON.parse(sessionStorage.getItem('actionHistorySave')) || [];
        console.log("actionHistorySave " + actionHistory + " - " + JSON.parse(sessionStorage.getItem('actionHistorySave')) || []);
        undoneHistory = JSON.parse(sessionStorage.getItem('undoneHistorySave')) || [];
        console.log("undoneHistorySave " + actionHistory + " - " + JSON.parse(sessionStorage.getItem('undoneHistorySave')) || []);


    }
    var inputData = JSON.parse(inputValue);
    for (var i = 0; i < inputData.nodesA.length; i++) {
        nodes.add(inputData.nodesA[i]);
    }
    for (var i = 0; i < inputData.edgesA.length; i++) {
        edges.add(inputData.edgesA[i]);
    }
    for (var i = 0; i < inputData.nodes_superA.length; i++) {
        nodes_super.add(inputData.nodes_superA[i]);
    }
    for (var i = 0; i < inputData.edges_superA.length; i++) {
        edges_super.add(inputData.edges_superA[i]);
    }
    updateTableElements();

}

function uploadData(fd) {
    $.ajax({
        type: 'POST',
        url: '/readFile',
        data: fd,
        processData: false,
        contentType: false,
        success: function (data) {
            var result = JSON.parse(data);
            if (result[0]) {
                $("#textoFileDrag").text(result[0]);
            } else {

                importNetwork("file", result[1]);
                $("#textoFileDrag").text(result[1]);
                $("[aria-label='Close']").click();
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            $("#textoFileDrag").text($("#textFileInvalid").text());
        }
    });
}

$(document).ready(function () {

    // Obtiene la información almacenada desde sessionStorage
    var data1 = sessionStorage.getItem('codeSave');

    if (data1) {
        importNetwork("session");
    }

    var layoutActive = sessionStorage.getItem('layoutActive');
    if (layoutActive) {
        $('.change-aparience[value="' + layoutActive + '"]').click()
    }

    $(".changeOptions").click(function () {
        exportNetwork("session");
    });

    $("#saveAs").click(function () {
        exportNetwork("file");
    });

    $("#loadFile").click(function () {
        if (nodes.get().length > 0 || nodes_super.get().length > 0) {
            var salir = confirm($("#textCerrarArchivo").text());
            if (salir) {
                dataExist();
            }
        } else {
            dataExist();
        }
    });
});


function dataExist() {
    $("[functioninsert='loadFile']").click();
    $("#formModalButton").hide();

    $("html").on("dragover", function (e) {
        e.preventDefault();
        e.stopPropagation();
        $("#textoFileDrag").text($("#textDragHere").text());
    });

    $("html").on("drop", function (e) {
        e.preventDefault();
        e.stopPropagation();
    });

    // Drag enter
    $('.upload-area').on('dragenter', function (e) {
        e.stopPropagation();
        e.preventDefault();
    });

    // Drag over
    $('.upload-area').on('dragover', function (e) {
        e.stopPropagation();
        e.preventDefault();
    });


    // Drop
    $('.upload-area').on('drop', function (e) {
        e.stopPropagation();
        e.preventDefault();

        var file = e.originalEvent.dataTransfer.files;
        var fd = new FormData();
        fd.append('file', file[0]);

        var doc = file[0].name.slice(0, -4);//para quitar el .dbw
        document.getElementById("docs-title").setAttribute('value', doc);
        uploadData(fd);
    });

    // Open file selector on div click
    $("#uploadfile").click(function () {
        $("#file").click();
    });

    // file selected
    $("#file").change(function () {
        var fd = new FormData();
        var files = $('#file')[0].files[0];

        fd.append('file', files);
        uploadData(fd);
    });
}