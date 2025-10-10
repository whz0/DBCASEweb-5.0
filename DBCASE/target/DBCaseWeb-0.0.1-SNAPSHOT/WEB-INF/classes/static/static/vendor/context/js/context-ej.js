$(document).ready(function(){
	var dataSelect;
	context.init({preventDoubleContext: false});
	
	var menu_options = [
		{text: $("#textaddNewEntity").text(), action: function(e){
			$( "[functioninsert='addEntity']").click();
		}},
		{text: $("#textaddNewRelation").text(), action: function(e){
			$( "[functioninsert='addRelation']").click();
		}},
		{text: $("#textaddNewRelationIsA").text(), action: function(e){
			$( "[functioninsert='addIsA']").click();
		}},
		{text: $("#textcreateDomain").text(), action: function(e){
			$( "[functioninsert='createDomain']").click();
		}},
		//entity
		{text: $("#addNewAttribute").text(), action: function(e){
			$( "[functioninsert='addAtribute']").click();
		}},
		{text: $("#textEditEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addEntity']").click();
			$( "#typeAction").val("edit");
			$("#insertModal").text($("#textAccept").text());
			idSele = $("#idSelected").val();
			fillEditEntity(idSele);
		}},
		{text: $("#textAddSuperEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addTextAgregation']").click();
		}},
		{text: $("#constraints").text(), href: '#', action: function(e){
			$( "[functioninsert='addConstraints']").click();
			idSele = $("#idSelected").val();
			if(!existConstraints(idSele)){
				fillEditConstraints(idSele);
			}
		}},
		{text: $("#tableUnique").text(), href: '#', action: function(e){
			$( "[functioninsert='addUniqueKey']").click();
			idSele = $("#idSelected").val();
			if(!existDataTableUnique(idSele)){
				fillEditTableUnique(idSele);//
			}
			$('#insertModal').prop('disabled', false);
		}},	
		// relation
		{text: $("#textAddEntitytoRelation").text(), href: '#', action: function(e){
				$( "#typeAction").val("create");
			$( "[functioninsert='addEntitytoRelation']").click();
			$('#insertModal').prop('disabled', false);

		}},
		{text: $("#textRemoveEntitytoRelation").text(), href: '#', action: function(e){
			$( "[functioninsert='removeEntitytoRelation']").click();
			$('#insertModal').prop('disabled', false);
			$('#insertModal').text($("#textRemove").text());
		}},
		{text: $("#textEditCardOrRol").text(), href: '#', action: function(e){
			$( "#typeAction").val("edit"); //cambiado
			$( "[functioninsert='addEntitytoRelation']").click();

			$( "#typeAction").val("edit");//cambio agrego aquí
			$('#insertModal').prop('disabled', false);
			$('#titleModal').html($('#textEditCardOrRol').text());
			$('#element_role').removeClass("d-none");
			$('#element_role').bind("change", function(){
				var edgS = edges.get($(this).val());
				$('input:radio[value="max'+edgS.labelTo+'"]').prop('checked', true);
				if(edgS.participation){
					$('#minCardinality').prop("disabled", false);
					$('#maxCardinality').prop("disabled", false);
					$('#minCardinality').val(edgS.participationFrom);
					$('#maxCardinality').val(edgS.participationTo);
				}else{
					$('#minCardinality').prop("disabled", true);
					$('#maxCardinality').prop("disabled", true);
					$('#minCardinality').val("");
					$('#maxCardinality').val("");
				}
				$('#roleName').val(edgS.name);
				$('#minMax').prop('checked',edgS.participation);
			});
			$('#element_role_label').removeClass("d-none");
			$("#insertModal").text($("#textAccept").text());

		}},
		{text: $("#addNewAttribute").text(), action: function(e){
			$( "[functioninsert='addAtribute']").click();
		}},
		{text: $("#textEditRelation").text(), href: '#', action: function(e){
			$( "[functioninsert='addRelation']").click();
			$( "#typeAction").val("edit");
			$("#insertModal").text($("#textAccept").text());
			idSele = $("#idSelected").val();
			fillEditRelation(idSele);
		}},
		{text: $("#textAddSuperEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addTextAgregation']").click();
		}},
		{text: $("#constraints").text(), href: '#', action: function(e){
			$( "[functioninsert='addConstraints']").click();
			idSele = $("#idSelected").val();
			if(!existConstraints(idSele)){
				fillEditConstraints(idSele);
			}
		}},
		{text: $("#tableUnique").text(), href: '#', action: function(e){
			$( "[functioninsert='addUniqueKey']").click();
			idSele = $("#idSelected").val();
			if(!existDataTableUnique(idSele)){
				fillEditTableUnique(idSele);//
			}
			$('#insertModal').prop('disabled', false);
		}},	
		// atributos
		{text: $("#renameEntity").text()+" Atributo", href: '#', action: function(e){
			$( "[functioninsert='addAtribute']").click();
			$("#insertModal").text($("#textAccept").text());
			$( "#typeAction").val("edit");
			idSele = $("#idSelected").val();
			fillEditAtributte(idSele);
		}},
		{text: $("#textAddSubAtributte").text(), href: '#', action: function(e){
			$( "[functioninsert='addSubAtribute']").click();
		}},
		//IsA
		{text: $("#textAddParentEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addEntityParent']").click();
			$('#insertModal').prop('disabled', false);
		}},
		{text: $("#textRemoveParentEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='removeParentIsA']").click();
		}},
		{text: $("#textAddChildEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addEntityChild']").click();
			$('#insertModal').prop('disabled', false);
		}},
		{text: $("#textRemoveChildEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='removeChildEntity']").click();
			$('#insertModal').prop('disabled', false);
		}},
		{text: $("#removeEntity").text(), href: '#', action: function(e){
			deleteNodeSelected(getNodeSelected());
		}},
		//superEntity
		// TODO: Añadir opciones
		{text: $("#textEditSuperEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='addTextAgregation']").click();
			$( "#typeAction").val("edit");
			$("#insertModal").text($("#textAccept").text());
			idSele = $("#idSelected").val();
			fillEditSuperEntity(idSele);
		}},
		{text: $("#deleteSuperEntity").text(), href: '#', action: function(e){
			$( "[functioninsert='deleteSuperEntity']").click();
			$('#insertModal').hide();
		}},
	];
	
	context.attach('#diagram', menu_options);
	context.settings({compress: true});
	
	$(document).on('mouseover', '.me-codesta', function(){
		$('.finale h1:first').css({opacity:0});
		$('.finale h1:last').css({opacity:1});
	});
	
	$(document).on('mouseout', '.me-codesta', function(){
		$('.finale h1:last').css({opacity:0});
		$('.finale h1:first').css({opacity:1});
	});
	
});