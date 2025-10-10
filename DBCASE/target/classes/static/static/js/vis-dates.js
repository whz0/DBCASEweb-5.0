var nodes = new vis.DataSet([]);
var nodes_super = new vis.DataSet([]);
var nodoSelected;
var poscSelection;
var typeDomain = new Domains();
// create an array with edges
var edges = new vis.DataSet([]);
var edges_super = new vis.DataSet([]);
var changeDrawView = true;
var nodes_selected_event = false;

var idCount =1000;
var idSuperEntityCount =0;
var actionHistory = [];
var undoneHistory = [];

 
  // create a network
var container = document.getElementById('diagram');
var container_super = document.getElementById('diagram_super');
var data_super = {
		nodes: nodes_super,
	    edges: edges_super
	};

var data = {
	nodes: nodes,
    edges: edges
};

var options = {
		
		 edges: {      
			// font: '22px arial #13A20E',
			/* font: {
				 //strokeWidth:0,
			      color: '#13A20E',
			      //size: 14px, // px
			      face: 'arial'},
*///borderWidthSelected:0,
		//	 font: '12px arial #000000',
		    smooth: {
		      type: "continuous",
		      forceDirection: "none",
		      roundness: 1
		    }
		  },
		  nodes: {
              borderWidth: 1.2,
			  font: '12px arial #000000',//cambiado
			  color: {
				 border: '#bf9523',
				 background:'#ffcc45', 
				 highlight: {
				        border: '#000000',
				        background: '#CEA023'
				      },
				 hover: {
					 border: '#ffcc45',
					 background: '#ffcc45'
						 }
			 }},
		  physics: {
	          enabled: false
	        },
		  interaction:{
		    dragNodes:true,
		    dragView: true,
		    hideEdgesOnDrag: false,
		    hideEdgesOnZoom: false,
		    hideNodesOnDrag: false,
		    hover: false,
		    hoverConnectedEdges: true,
		    keyboard: {
		      enabled: true,
		      speed: {x: 10, y: 10, zoom: 0.02},
		      bindToWindow: false
		    },
		    multiselect: true,
		    navigationButtons: true,
		    selectable: true,
		    selectConnectedEdges: true,
		    tooltipDelay: 300,
		    zoomView: true
		  }

		};
  
//options = {};
var network = new vis.Network(container, data, options);

var network_super = new vis.Network(container_super, data_super, options);

/**
 * 
 * @returns Devuelve un id unico para asignar a un nuevo elemento que se cree
 */
  function getIdElement(){      //TODO: Eliminar, verificar antes su uso
	  var dataIds = nodes.getIds();
	  if(dataIds.length==0)
		  var nextId = -1;
	  else
		  var nextId = dataIds[dataIds.length-1];
	  return ++nextId;
  }
  
  function deleteSuperEntity(idNodo){
    console.log("deleting superEntity");
	  var idNode = parseInt(idNodo);
	  var superNode = nodes.get(idNode);

	  actionHistory.push({ type: 'startSuperEntityDelete', node: null});
      console.log("[actionHistory] - startSuperEntityDelete ");

	  nodes_super.forEach(function(nod) {
		  // Desmarcamos los elementos que forman parte de la entidad
		  if(nod.superEntity==idNode){
              actionHistory.push({ type: 'deleteFromSuperEntity', node: JSON.parse(JSON.stringify(nod)) });
              console.log("[actionHistory] - deleteFromSuperEntity: " + nod.label);
              nodes.update({id:nod.id, superEntity:-1});
              nodes_super.remove(nod.id);
		  }

	  });

	  actionHistory.push({ type: 'deleteSuperEntity', node: JSON.parse(JSON.stringify(superNode)) });
	  console.log("[actionHistory] - deleteSuperEntity: " + superNode.label);
      actionHistory.push({ type: 'stopSuperEntityDelete', node: null});
      console.log("[actionHistory] - stopSuperEntityDelete ");
      clearUndoneHistory();
	  nodes.remove(idNode);

	  updateTableElements();
  }
  
  function deleteSuperEntityAndEelements(idNodo){
	  var idNode = parseInt(idNodo);
	  var superNode = nodes.get(idNode);
	  console.log("deleteSuperEntityAndEelements: "+ idNode);
	  actionHistory.push({ type: 'startSuperEntityDelete', node: null});
      console.log("[actionHistory] - startSuperEntityDelete ");

	  nodes_super.forEach(function(nod) {

          // Eliminamos los nodos que forman parte de la agregación
          if(nod.superEntity==idNode){
              actionHistory.push({ type: 'deleteWithSuperEntity', node: JSON.parse(JSON.stringify(nod)) });
              console.log("[actionHistory] - deleteWithSuperEntity: " + nod.label);
              nodes.remove(nod.id);
              nodes_super.remove(nod.id);
          }
      });

      // Eliminamos la agregación
      actionHistory.push({ type: 'deleteSuperEntity', node: JSON.parse(JSON.stringify(superNode)) });
      console.log("[actionHistory] - deleteSuperEntity: " + superNode.label);

      actionHistory.push({ type: 'stopSuperEntityDelete', node: null});
      console.log("[actionHistory] - stopSuperEntityDelete ");
      clearUndoneHistory();

	  nodes.remove(idNode);

  }

  function ctxRenderer({ ctx, x, y, state: { selected, hover }, style , label}) {
      const r = style.size;
      ctx.beginPath();
      const sides = 6;
      const a = (Math.PI * 2) / sides;
      ctx.moveTo(x , y + r);
      for (let i = 1; i < sides; i++) {
          ctx.lineTo(x + r * Math.sin(a * i), y + r * Math.cos(a * i));
      }
      ctx.closePath();
      ctx.save();
      ctx.fillStyle = 'red';
      ctx.fill();
      ctx.stroke();
      ctx.restore();

      ctx.font = "normal 12px sans-serif";
      ctx.fillStyle = 'black';
  }
  
  function simuleClickSuper(){  //TODO: Eliminar funcion
		var event = new PointerEvent('pointerdown');
		return new Promise(resolve => document.getElementsByClassName("vis-zoomExtendsScreen")[0].dispatchEvent(event));
  }
  
  function createSuperEntity(labelName){    //TODO: Eliminar funcion
	  var size_width = 110;
	  var c = document.getElementsByTagName("canvas")[0];
	  var sizeWidth = c.style.width.slice(0, -2);
	  if(parseInt(sizeWidth)<120)
		  size_width = sizeWidth;
	  
	  if(nodes_super.get().length<5)
		  size_width = 45;
	  var ctx = c.getContext("2d");

	  var textTheme = $("#textTheme").text();
      var isDarkTheme = (textTheme === 'dark');

  }
  
  async function simuleClickAsync() {   //TODO: Eliminar funcion
	  let promise = new Promise((resolve, reject) => {
	    setTimeout(() => resolve("done!"), 200)
	  });
	  let result = await promise; // wait until the promise resolves (*)
	  await simuleClickSuper(); // "done!"
	}
  
  async function simuleClickAsync1() {      //TODO: Eliminar funcion
	  let promise = new Promise((resolve, reject) => {
	    setTimeout(() => resolve("done!"), 2000)
	  });
	  let result = await promise; // wait until the promise resolves (*)
	  await simuleClickSuper(); // "done!"
	}
  
  function simuleClickSuperNew(){       //TODO: Eliminar funcion
	  var left = 0;
	  var right = 0;
	  var top = 0;
	  var bottom = 0;
	  if(nodes_super.length>0){
		  left = nodes_super.get()[0].x;
		  right = nodes_super.get()[0].x;
		  top = nodes_super.get()[0].y;
		  bottom = nodes_super.get()[0].y;
	  }

	  nodes_super.forEach(function(nod) {
		  if(left>nod.x){
			  left = nod.x;
		  }
	  });
	  
	  nodes_super.forEach(function(nod) {
		  if(right<nod.x){
			  right = nod.x;
		  }
	  });
	  
	  nodes_super.forEach(function(nod) {
		  if(top>nod.y){
			  top = nod.y;
		  }
	  });
	  
	  nodes_super.forEach(function(nod) {
		  if(bottom<nod.y){
			  bottom = nod.y;
		  }
	  });
	  var width_super = right - left;
	  var height_super = top - bottom;
	  width_super = Math.abs(width_super);
	  height_super = Math.abs(height_super)+50;

	  var widthTotal = (width_super+50);
	  var heightTotal = ((height_super*(width_super+50))/width_super);

	  document.getElementsByTagName("canvas")[0].style.width = widthTotal+"px";
	  document.getElementsByTagName("canvas")[0].style.height = heightTotal+"px";
  }
  
  async function simuleClickAsyncNew() {    //TODO: Eliminar funcion
	  let promise = new Promise((resolve, reject) => {
	    setTimeout(() => resolve("done!"), 1000)
	  });
	  let result = await promise;
	  await simuleClickSuperNew();
	  console.log("prueba12");
  }
  
  async function simuleClickAsync12(labelName) {        //TODO: Eliminar funcion
	  let promise = new Promise((resolve, reject) => {
	    setTimeout(() => resolve("done!"), 2800)
	  });
	  let result = await promise;
	  await createSuperEntity(labelName);
	}
  
  async function updateTableElementsPromise() {
	  let promise = new Promise((resolve, reject) => {
	    setTimeout(() => resolve("done!"), 3000)
	  });
	  let result = await promise;
	  await updateTableElements();
	}

    function updateIdCount(){
        //Actualizamos el contador
        if (idCount-1000 < nodes.length- idSuperEntityCount){
            var nods = nodes.getIds();
            var idCountAux = idCount;
            nods.forEach(function(node) {
                if(node >= idCount){
                    idCount = node;
                    idCount++;
                }
            });
        }
    }
    function updateidSuperEntityCount(){
        //Actualizamos el contador
        var nods = nodes.getIds();
        nods.forEach(function(node) {
            if(node < 1000 && node >= idSuperEntityCount){
                idSuperEntityCount = node;
                idSuperEntityCount++;
            }
        });

    }
  
    function addSuperEntity(idElement, labelName, action){

        console.log("[Super Entity] - idCount: " + idCount + ", label: " + labelName + ", idSuperEntityCount: " + idSuperEntityCount + " - idSelected: " + idElement);
        updateidSuperEntityCount()

        if(action == "edit"){
            actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(idElement))) });
            console.log("[actionHistory] - modify: " + labelName);
            nodes.update({id:idElement, label:labelName});
        }
        else{   // No hay una agregación ya creada

            var selectedNodes = network.getSelectedNodes();
            if (!selectedNodes.includes(idElement)){
                selectedNodes.push(idElement);
                network.selectNodes(selectedNodes);
            }

            if(getNodesElementsWithSuperEntity(network.getSelectedNodes(), idSuperEntityCount)){
                //get nodes connected to super entity
                updateSuperEntityEdges();

                var textTheme = $("#textTheme").text();
                var isDarkTheme = (textTheme === 'dark');
                var data_element = {id: idSuperEntityCount, widthConstraint: { minimum: 400}, heightConstraint: { minimum: 200 }, label: labelName, shape: 'box',physics:false, IsSuperEntity:true, superEntity:-1,
                  color:{
                      border: '#ffcc45',
                      background: 'transparent',
                      highlight: {
                          border: '#000000',
                          background: 'transparent'
                      }
                  },
                   borderWidth: 2, font: {
                       color: isDarkTheme ? '#000000' : '#ffffff',
                  }
                };

                setSuperEntityCoordinates(false, data_element);

                idSuperEntityCount++;
                idCount++;
            }
        }
      updateTableElementsSuperEntity();
    }



function setSuperEntityCoordinates(modifySuperEntity, node){

    var left = null;
    var right = null;
    var top = null;
    var bottom = null;

    var x_super = 0;
    var y_super =0;
    var width_super = 0;
    var height_super = 0;

    var allNodes;

    if(nodes_super.length == 0) nodes.remove(getSuperEntityNode());
    else{
        allNodes= nodes.get();

        allNodes.forEach(function(nod){
            // Guardamos las coordenadas que forman los extremos de los nodos de la agregación

            if(nod.superEntity == node.id){
                if(left ===null || left > nod.x){
                    left = nod.x;
                }

                if(right ===null || right < nod.x) {
                    right = nod.x;
                }

                if(top == null || (top > nod.y)) {
                    top = nod.y;
                }

                if(bottom ===null || (bottom < nod.y)) {
                    bottom = nod.y;
                }

            }

        });

        node.x = (left + right)/2;
        node.y = (top + bottom)/2;

        node.widthConstraint.minimum = (Math.abs(right - left)) + 200;
        node.heightConstraint.minimum = (Math.abs(top - bottom)) + 100;

        node.font.vadjust = (node.heightConstraint.minimum/2) + 15;


        if(!modifySuperEntity && node.IsSuperEntity){  // Añadimos agregación u otro elemento nuevo
            actionHistory.push({ type: 'addSuperEntity', node: JSON.parse(JSON.stringify(node)) });
            console.log("[actionHistory] - addSuperEntity: " + node.label);
            nodes.add(node);
            //nodes_super.add(node);
        }
        else{       // Modificamos un nodo
            nodes.update(node);

            // TODO: Replantear o eliminar
            if(node.superEntity >= 0){
                nodes_super.update(node);
            }
        }
    }
}

  // Marcamos los nodos que pertenecen a la agregación
  function getNodesElementsWithSuperEntity(nodesIds, superEntityId){

      var valid = true;
      if(nodesIds.length > 0){
          actionHistory.push({ type: 'startAddToNewSuperEntity', node: null });
          console.log("[actionHistory] - startAddToNewSuperEntity");

          for(var i = 0; i < nodesIds.length; i++) {
          //Actualizamos el campo si el nodo no es la agregación
              var nod = nodesIds[i];
              var node = nodes.get(nod);

              if(!node.IsSuperEntity && node.superEntity < 0){

                  // Buscamos los atributos de las entidades y relaciones que forman parte de la agregación
                  switch(node.shape){

                  case 'diamond':
                      node.superEntity = superEntityId;
                      actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(node)) });
                      console.log("[actionHistory] - addToNewSuperEntity: " + node.label);
                      nodes.update(node);
                      nodes_super.update(node);

                      //Recorremos y actualizamos las entidades conectadas a la relación, si el nodo no está seleccionado
                      var entitiesOfRelation = allEntityOfRelation(node.id);
                      entitiesOfRelation.forEach(function(eRelation){
                        //Actualizamos el campo si el nodo no está seleccionado
                        if(!nodes.get(eRelation.id).IsSuperEntity){
                            if(nodes.get(eRelation.id).superEntity < 0){
                              eRelation.superEntity = superEntityId;
                              nodes.update(eRelation);
                              actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(eRelation.id))) });
                              console.log("[actionHistory] - addToNewSuperEntity: " + eRelation.label + " - " + nodes.get(eRelation.id).superEntity);

                              var auxE = nodes.get(eRelation.id);
                              nodes_super.add(auxE);

                              var relationAttr = allAttributeOfEntity(eRelation.id);
                              relationAttr.forEach(function(attr){
                                  //Actualizamos el campo si el nodo no está seleccionado

                                  attr.superEntity = superEntityId;
                                  nodes.update(attr);
                                  actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(attr.id))) });
                                  console.log("[actionHistory] - addToNewSuperEntity: " + attr.label);
                                  var aux = nodes.get(attr.id);
                                  nodes_super.add(aux);

                                  var subAttr = allSubAttribute(attr.id);
                                  subAttr.forEach(function(sAttr){
                                     //Actualizamos el campo si el nodo no está seleccionado
                                     sAttr.superEntity = superEntityId;
                                     nodes.update(sAttr);
                                     actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(sAttr.id))) });
                                     console.log("[actionHistory] - addToNewSuperEntity: " + sAttr.label);

                                     var auxS = nodes.get(sAttr.id);
                                     nodes_super.add(auxS);
                                  });

                                });
                            }
                        }
                        else if(node.superEntity >= 0){
                          actionHistory.push({ type: 'stopAddToNewSuperEntity', node: null });
                          console.log("[actionHistory] - stopAddToNewSuperEntity");
                          undoLastAction();

                          alert($('#textErrorRelationAggr').text());
                          valid = false;
                          return valid;
                        }
                      });
                          var relationAttr = allAttributeOfEntity(node.id);
                          relationAttr.forEach(function(attr){
                            //Actualizamos el campo si el nodo no está seleccionado

                            attr.superEntity = superEntityId;
                            nodes.update({id: attr.id, superEntity: superEntityId});
                            actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(attr.id))) });
                            console.log("[actionHistory] - addToNewSuperEntity: " + attr.label);

                            var aux = nodes.get(attr.id);
                            nodes_super.add(aux);

                            //Recorremos y actualizamos los subatributos si los hay
                            var subAttr = allSubAttribute(attr.id);
                            subAttr.forEach(function(sAttr){
                             //Actualizamos el campo si el nodo no está seleccionado
                                sAttr.superEntity = superEntityId;
                                nodes.update({id: sAttr.id, superEntity: superEntityId});
                                actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(sAttr.id))) });
                                console.log("[actionHistory] - addToNewSuperEntity: " + attr.label);

                                var auxS = nodes.get(sAttr.id);
                                nodes_super.add(auxS);
                             });

                          });

                  break;
                  default:
                    console.log("Node no es una entidad ni una relacion: " + node.shape + " - " + node.label);
                  break;
                  }
              }
              else if(node.superEntity >= 0){
                  actionHistory.push({ type: 'stopAddToNewSuperEntity', node: null });
                  console.log("[actionHistory] - stopAddToNewSuperEntity");
                  undoLastAction();
                  alert($('#textErrorCreatingAggr').text());

                  return false;
              }

              else if(node.IsSuperEntity){
                  actionHistory.push({ type: 'stopAddToNewSuperEntity', node: null });
                  console.log("[actionHistory] - stopAddToNewSuperEntity");
                  undoLastAction();
                  alert($('#textErrorAggrInsideAggr').text());
              }
          }

          return valid;
	  }
	  else{
        return false;
	  }
  }

  function addConnectedEntitiesToSuperEntity(idNode){
      //Recorremos y actualizamos las entidades conectadas a la relación, si el nodo no está seleccionado
      var superEntityId = nodes.get(idNode).superEntity;
      var entitiesOfRelation = allEntityOfRelation(idNode);
      entitiesOfRelation.forEach(function(eRelation){
        //Actualizamos el campo si el nodo no está seleccionado

        if(eRelation.superEntity <0){
          actionHistory.push({ type: 'addToSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(eRelation.id))) });
          console.log("[actionHistory] - addToSuperEntity: " + eRelation.label);
          nodes.update({id: eRelation.id, superEntity: superEntityId});
          var auxE = nodes.get(eRelation.id);
          nodes_super.add(auxE);

            var relationAttr = allAttributeOfEntity(eRelation.id);
            console.log(relationAttr.length);
            relationAttr.forEach(function(atr){
            //Actualizamos el campo si el nodo no está seleccionado
                var attr = nodes.get(atr.id);
                if(attr.superEntity < 0){
                    actionHistory.push({ type: 'addToSuperEntity', node: JSON.parse(JSON.stringify(attr)) });
                    console.log("[actionHistory] - addToSuperEntity: " + attr.label);
                    nodes.update({id: attr.id, superEntity: superEntityId});
                    var aux = nodes.get(attr.id);
                    nodes_super.add(aux);
                }
                var subAttr = allSubAttribute(attr.id);
                subAttr.forEach(function(sAtr){
                    var sAttr = nodes.get(sAtr.id);
                    //Actualizamos el campo si el nodo no está seleccionado
                    if(sAttr.superEntity <0){
                       actionHistory.push({ type: 'addToSuperEntity', node: JSON.parse(JSON.stringify(sAttr)) });
                       console.log("[actionHistory] - addToSuperEntity: " + sAttr.label);
                       nodes.update({id: sAttr.id, superEntity: superEntityId});
                       var auxS = nodes.get(sAttr.id);
                       nodes_super.add(auxS);
                    }
                });

            });
        }

      });
      actionHistory.push({ type: 'stopAddToSuperEntity', node: superEntityId });
      console.log("[actionHistory] - stopAddToSuperEntity ");
  }

  function updateSuperEntityEdges(){

      var edge = edges.get();
      var superNodes = nodes_super.getIds();
      var superEdges = edges_super.getIds();
  	  edge.forEach(function(edg) {
  	  // Añadimos los edges de los elementos que forman parte de la agregación
  		  if(!superEdges.includes(edg.id) && (superNodes.includes(edg.to) && superNodes.includes(edg.from))){
              edges_super.add(edg);
  		  }
  	  });
    }
  
  function addEntity(nombre, weakEntity,action, idSelected, elementWithRelation, relationEntity){
	  updateIdCount();

      var isWeakEntity =false;

      if(action == "edit") isWeakEntity = (weakEntity && !nodes.get(parseInt(idSelected)).isWeak);
      else isWeakEntity = weakEntity;

	  console.log("[Entity] - idCount: " + idCount + ", label: " + nombre + ", idSuperEntityCount: " + idSuperEntityCount + ", nodes size: " + nodes.length);
	  var data_element = {id: idCount, widthConstraint:{minimum: 100, maximum: 200}, label: nombre, isWeak: weakEntity, shape: 'box', scale:10, heightConstraint:25,physics:true, IsSuperEntity:false, superEntity:-1};//cambiado

      if(isWeakEntity){
          actionHistory.push({ type: 'startWeakEntity', node: null });
          console.log("[actionHistory] - startWeakEntity: " + data_element.label);
      }

	  if(action == "edit"){
		  data_element.id = parseInt(idSelected);
          data_element.superEntity=nodes.get(data_element.id).superEntity;

          actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
          console.log("[actionHistory] - modify: " + data_element.label);

		  nodes.update(data_element);

	  }else{

		  if(poscSelection != null){
			  data_element.x = poscSelection.x;
			  data_element.y = poscSelection.y;
		  }

		  nodes.add(data_element);
		  actionHistory.push({ type: 'addNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
		  console.log("[actionHistory] - addNode: " + data_element.label);
		  clearUndoneHistory();
		  idCount++;
	  }
	  
	  if(weakEntity && elementWithRelation != null){
		  idRelation = addRelation(relationEntity, "create", null, "back");
		  addEntitytoRelation(data_element.id, "", "1to1", "", "1", "1", "create", idRelation, true);
		  addEntitytoRelation(parseInt(elementWithRelation), "", "1toN", "", "1", "N", "create", idRelation, false);
	  }

      if(isWeakEntity){
        actionHistory.push({ type: 'stopWeakEntity', node: null });
        console.log("[actionHistory] - stopWeakEntity: " + data_element.label);
      }

	  updateTableElements();
  }

  function addConstraints(values, idSelected, action){
	  var valuesFilter = [];
	  for(var i=0;i<values.length;i++){
		 if(values[i].value!="" && values[i].value!="${temp_value}")
			  valuesFilter.push(values[i].value);
	  }
	  var data_element = {constraints: valuesFilter};
	  data_element.id = parseInt(idSelected);
	  nodes.update(data_element);
  }
 
  function addTableUnique(values, idSelected, action){
	  var data_element = {tableUnique: JSON.stringify(values)};
	  data_element.id = parseInt(idSelected);
	  nodes.update(data_element);
  }
  
  function addRelation(nombre, action, idSelected, origin = "front"){

	  var  tam = 30;
	  if (nombre.length>5){
		  tam = 30+(nombre.length-5);
	  }
	  console.log("[Relation] - idCount: " + idCount + ", label: " + nombre + ", idSuperEntityCount: " + idSuperEntityCount);
	  updateIdCount();
	  var data_element = {id: idCount, size:tam,label: nombre, shape: 'diamond', IsSuperEntity:false, superEntity:-1,
		  color: {
				 border: '#c9280e',
				 background:'#FF3F20',
				 highlight: {
				        border: '#000000',
				        background: '#C93821'
				      }}
		  //color: '#FF3F20'
		  , scale:20, physics:false, zIndex:0};//D5FF04  cambiado(ff554b)
	  
	  if(action == "edit"){
		  data_element.id = parseInt(idSelected);
		  data_element.superEntity=nodes.get(data_element.id).superEntity;
		  actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
		  console.log("[actionHistory] - modify: " + data_element.label);
		  nodes.update(data_element);
	  }else{

		  if(poscSelection != null){
			  if(origin != "front"){
				  data_element.x = poscSelection.x;
				  data_element.isWeak = "active";
				  data_element.y = poscSelection.y-100;
			  }else{
				  data_element.x = poscSelection.x;
				  data_element.y = poscSelection.y;
			  }
		  }
		  nodes.add(data_element);
		  actionHistory.push({ type: 'addNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
		  console.log("[actionHistory] - addNode: " + data_element.label);
		  clearUndoneHistory();
		  idCount++;
	  }
	  if(origin != "front"){
		  return data_element.id;
	  }
	  updateTableElements();
  }
  
  function addIsA(){    //TODO: Añadir opcion de editar en todas las opciones del elemento
      updateIdCount();
	  var data_element = {id: idCount, label: 'IsA', shape: 'triangleDown',IsSuperEntity:false, superEntity:-1,
          color: {
                 border: '#FF952A',
                 background:'#FF952A',
                 highlight: {
                        border: '#000000',
                        background: '#D37211'
                      }}
          , scale:20, physics:false};

      /*if(action == "edit"){
          data_element.id = parseInt(idSelected);
          data_element.super_entity=nodes.get(data_element.id).super_entity;
          actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
          nodes.update(data_element);
      }*/
      //else{

          if(poscSelection != null){
              data_element.x = poscSelection.x;
              data_element.y = poscSelection.y;
          }

          nodes.add(data_element);
          actionHistory.push({ type: 'addNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
          console.log("[actionHistory] - addNode: " + data_element.label);
          clearUndoneHistory();
	  //}
	  idCount = idCount + 1;
	  updateTableElements();
  }
  
  function addAttribute(name, action, idSelected, idEntity, pk, comp, notNll, uniq, multi, dom, sz){
	  var word_pk = name;
	  if(pk){
		  word_pk = name;
	  }else{
		  word_pk = name;
		  if(!notNll){
			  word_pk +="*";
		  }
	  }
	  var valueEntityWeak = nodes.get(parseInt(idEntity)).isWeak;
      updateIdCount();
	  console.log("[Attribute] - idCount: " + idCount + ", label: " + name + ", idSuperEntityCount: " + idSuperEntityCount);
	  var data_element = {id: idCount, width: 3,widthConstraint:{ minimum: 50, maximum: 160},labelBackend:name, label: word_pk, dataAttribute:{entityWeak: valueEntityWeak, primaryKey: pk, composite: comp, notNull: notNll, unique: uniq, multivalued: multi, domain: dom, size: sz}, shape: 'ellipse', IsSuperEntity:false, superEntity:-1,
		  		  color: {
					 border: '#078980',
					 background:'#22bdb1',
					 highlight: {
					        border: '#000000',
					        background: '#1A958A'
					      }},
					      scale:20, heightConstraint:23,physics:false};

	  if(action == "edit"){
		  data_element.id = parseInt(idSelected);
		  data_element.dataAttribute.entityWeak = nodes.get(parseInt(idSelected)).dataAttribute.entityWeak;
		  data_element.superEntity=nodes.get(data_element.id).superEntity;
		  actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
		  console.log("[actionHistory] - modify: " + data_element.label);
		  nodes.update(data_element);
		  if(data_element.superEntity >= 0) {
              nodes_super.update(data_element);
              setSuperEntityCoordinates(true, nodes.get(data_element.superEntity));
          }
	  }else{

		  if(poscSelection != null){
			  data_element.x = poscSelection.x-180;
			  data_element.y = poscSelection.y+30;
		  }

          //Añadimos atributo a agregación
		  if(inSuperEntity(parseInt(idEntity))){
              data_element.superEntity = nodes.get(parseInt(idEntity)).superEntity;
              //Añadimos el nodo
              nodes.add(data_element);
              nodes_super.add(data_element);

              actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
              console.log("[actionHistory] - addToNewSuperEntity: " + data_element.label);

              // Añadimos los edges
              edges.add({from: parseInt(idEntity), to: parseInt(idCount), color:{color:'#22bdb1'},width: 2});//cambiado
              setSuperEntityCoordinates(true, nodes.get(data_element.superEntity));
              updateSuperEntityEdges();

		  }
		  // Añadimos atributo fuera de la agregación
		  else{
              nodes.add(data_element);
              actionHistory.push({ type: 'addNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
              console.log("[actionHistory] - addNode: " + data_element.label);
              clearUndoneHistory();
              edges.add({from: parseInt(idEntity), to: parseInt(idCount), color:{color:'#22bdb1'},width: 2});//cambiado
		  }
          idCount++;
	  }

	  updateTableElements();
  }
  
  function addEntitytoRelation(idTo, element_role, cardinality, roleName, minCardinality, maxCardinality, action, idSelected, partActive){

	  var left;
	  var center = roleName;
	  var labelText = center
	  var right;
	  var exist = false;
	  var direct1 = false;
	  switch(cardinality){
	  	case 'max1':
	  		direct1 = true;
	  		left = '1';
	  		right = '0';
	  	break;
	  	case 'maxN':
		  	left = 'N';
	  		right = '0';
	  	break;
	  	case '1toN':
	  		direct1 = true;
		  	left = 'N';
	  		right = '1';
	  	break;
	  	case '1to1':
		  	left = '1';
	  		right = '1';
	  	break;
	  	case 'minMax':
		  	left = maxCardinality;
	  		right = minCardinality;
	  	break;
	  	default:
	  }

      var idEdge = existEdge(idSelected, idTo, element_role);

	  if(roleName == "" && idEdge != null && edges.get(idEdge).label != ""){
		  center = " ";
      }

	  if(partActive){
		  labelText = "( "+minCardinality+" , "+maxCardinality+" ) "+ center;
	  }else{
		  labelText = center;
	  }

	  var data_element = {width: 3,from: parseInt(idSelected), to: parseInt(idTo), label: labelText, labelFrom:right, labelTo:left, name:center, participation:partActive ,participationFrom: minCardinality, participationTo: maxCardinality, state: "false", smooth:false,arrows:{to: { enabled: direct1 }}};
	  var data_element1 = {width: 3,from: parseInt(idSelected), to: parseInt(idTo), label: labelText, labelFrom:right, labelTo:left, name:center, participation:partActive ,participationFrom: minCardinality, participationTo: maxCardinality, state: "false", smooth:false ,arrows:{to: { enabled: direct1 }}};
	  var data_element_update = {};
	  var data_element3 = {};

	  if(action == "edit"){

          var idOther = existOtherEdge(idSelected, idTo, element_role);

          data_element.id = idEdge;
          data_element.state = edges.get(idEdge).state;
          data_element.name = edges.get(idEdge).name;

          if(nodes.get(data_element.to).IsSuperEntity) data_element.color = '#7e7978';

          actionHistory.push({ type: 'modifyEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(idEdge))) });
          console.log("[actionHistory] - modifyEntityToRelation: " + edges.get(idEdge).from + " - " + edges.get(idEdge).to + " - id: "+idEdge+ " --> FLECHAS: "+ edges.get(idEdge).state);
		  edges.updateOnly(data_element);

		  if(idOther != null) console.log("not updated: " + edges.get(idOther).from + " - " + edges.get(idOther).to + " - id: "+edges.get(idOther).id+ " --> FLECHAS: "+ edges.get(idOther).state + " - "+ edges.get(idOther).arrows.to.enabled+ " name:" +edges.get(idOther).name);


		  if(inSuperEntity(idTo)){
		    edges_super.update(data_element);
		  }

	  }else{
		  if(idEdge != null){

              actionHistory.push({ type: 'addEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(idEdge))) });
              console.log("[actionHistory] - addEntityToRelation: " + edges.get(idEdge).from + " - " + edges.get(idEdge).to + " - id: "+idEdge + " - state: "+ edges.get(idEdge).state + " name:" +edges.get(idEdge).name);
			  data_element_update = edges.get(idEdge);
			  data_element_update.state = "right";
			  data_element1.state = "left";

              if(nodes.get(data_element1.to).IsSuperEntity) data_element1.color = '#7e7978';

			  edges.update(data_element_update);
			  console.log("[actionHistory] - addEntityToRelation: " + edges.get(idEdge).from + " - " + edges.get(idEdge).to + " - id: "+idEdge + " - state: "+ edges.get(idEdge).state + " name:" +edges.get(idEdge).name);

			  edges.add(data_element1);
              actionHistory.push({ type: 'addNewEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(data_element1.id))) });
              console.log("[actionHistory] - addNewEntityToRelation: " + edges.get(data_element1.id).from + " - " + edges.get(data_element1.id).to + " - id: "+edges.get(data_element1.id).id + " - state: "+ edges.get(data_element1.id).state + " name:" +edges.get(data_element1.id).name);
			  if(inSuperEntity(idTo)){
                edges_super.update(data_element_update);
                edges_super.add(data_element1);
              }

		  }else{

              if(nodes.get(data_element.to).IsSuperEntity) data_element.color = '#7e7978';

			  edges.add(data_element);

              actionHistory.push({ type: 'addNewEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(data_element.id))) });
              console.log("[actionHistory] - addNewEntityToRelation: " + data_element.from + " - " + data_element.to + " - id: "+data_element.id + " name:" +data_element.name);
			  if(inSuperEntity(data_element.from)){

                addConnectedEntitiesToSuperEntity(data_element.from);
                edges_super.add(data_element);
                var nodeAux = nodes.get(data_element.from);
                setSuperEntityCoordinates(true, nodes.get(nodeAux.superEntity));

              }

		  }  
	  }
  }
  
  /**
   * Añadir una entidad padre a un elemento IsA
   * @param idTo Entidad Padre
   * @param action añadir o actualizar
   * @param idSelected Nodo IsA
   * @returns
   */
  function addEntityParent(idTo, action, idSelected){
    console.log("< -- addEntityParent -- >");
	  var idParent = nodes.get(parseInt(idSelected)).parent;
	  var data_element = {width: 3,from: parseInt(idSelected), to: parseInt(idTo),type:"parent", arrows: 
	  						{from: { enabled: true }, middle: { enabled: false },to: { enabled: false }
	  						}
	  					};
	  
	  if(idParent != null){
		  var idEdge = existEdge(parseInt(idSelected), idParent, null);
		  data_element.id = idEdge;
		  edges.update(data_element);
	  }else{
		  edges.add(data_element);
          actionHistory.push({ type: 'addEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(data_element.id))) });
          console.log("[actionHistory] - addEntityToRelation: " + edges.get(data_element.id).from + " - " + edges.get(data_element.id).to);
	  }
	  nodes.update({id: parseInt(idSelected), parent: parseInt(idTo)});
	  updateTableElements();
  }
  
  /**
   * Quita la entidad padre
   * @param idNodo Id padre
   * @returns
   */
  function removeParentIsA(idNodo){
	  var isA = nodes.get(parseInt(idNodo));
	  var idParent = isA.parent;
      actionHistory.push({ type: 'stopDeleteIsA', edge: null });
      console.log("[actionHistory] - stopDeleteIsA");
      actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(isA)) });
      console.log("[actionHistory] - modifyNode: " + isA.label);
	  nodes.get(parseInt(idNodo)).parent = undefined;
	  var allData = allEntitysToRelation(idNodo);
	  
	  allData.forEach(function (key){
		  if(nodes.get(idParent).label == key.label){
              actionHistory.push({ type: 'deleteIsARelation', edge: JSON.parse(JSON.stringify(edges.get(key.id))) });
              console.log("[actionHistory] - deleteIsARelation: " + key.label);
			  edges.remove(key.id);
          }
	  });
      actionHistory.push({ type: 'startDeleteIsA', edge: null });
      console.log("[actionHistory] - startDeleteIsA");
	  nodes.update({id: parseInt(idNodo), parent: undefined});
	  updateTableElements();
  }
  
  function removeEntitytoRelation(idEdge, action, idSelected){

	  var idFrom = edges.get(idEdge).from;
	  var idTo = edges.get(idEdge).to;

      actionHistory.push({ type: 'deleteEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(idEdge))) });
      console.log("[actionHistory] - deleteEntityToRelation: " + idFrom + " - " + idTo);

	  edges.remove(idEdge);
	  var idExist = existEdge(idFrom, idTo, null);
	  if(idExist != null){

		  var data_element_update = {};
		  data_element_update.id = idExist;
		  data_element_update.state = "false";
          actionHistory.push({ type: 'modifyOtherEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(idExist))) });
          console.log("[actionHistory] - modifyOtherEntityToRelation: " + edges.get(idExist).from + " - " + edges.get(idExist).to + " - state: " + edges.get(idExist).state);
		  edges.update(data_element_update);
          console.log("[actionHistory] - modifyOtherEntityToRelation2: " + edges.get(idExist).from + " - " + edges.get(idExist).to + " - state: " + edges.get(idExist).state);

	  }
	  updateTableElements();
  }
  
  /* 
   * filter = array
   * if (filter = null) return allNodes 
   * else return nodes of type filter
   * */
  function getAllNodes(filter = null){
	  var data = [];
	  if(filter != null){
		  nodes.forEach(function(nod) {
			  if(filter.indexOf(nod.shape) != -1)
				  data.push(nod);				  
		  });
	  }else{
		  nodes.forEach(function(nod) {
			  data.push(nod);
		  });
	  }
	  return data;
  }

    function getEntitiesNotInSuperEntity(node){
      var data = [];
      nodes.forEach(function(nod) {

       if(nod.shape == "box"){
            if(!nod.IsSuperEntity && (nod.superEntity < 0 || nod.superEntity == node)) {
                data.push(nod);
            }
        }
      });
      return data;
    }
  
  /* 
   * filter = array
   * if (filter = null) return allNodes 
   * else return nodes of type filter
   * */
  function getAllNodesSuper(filter = null){
	  var data = [];
	  if(filter != null){
		  nodes.forEach(function(nod) {
			  if(filter.indexOf(nod.shape) != -1)
				  data.push(nod);				  
		  });
	  }else{
		  nodes_super.forEach(function(nod) {
			  data.push(nod);
		  });
	  }
	  return data;
  }
  
  /*
   * Check if exist a edge between "idFrom" to "idTo" nodes
   * return "null" if it doesn't exist
   * return idEdge if it  exist
   * */
  function existEdge(idFrom, idTo, filter = null){
	  var idEdgeExist = null;
	  var edgesFrom = network.getConnectedEdges(parseInt(idFrom));
	  var edgesTo = network.getConnectedEdges(parseInt(idTo));
	  var dataPush = [];
	  edgesTo.forEach(function(idEdge) {

          if(filter == null){
              if(edgesFrom.indexOf(idEdge) != -1)
                idEdgeExist = idEdge;
          }
          else{
              if(edgesFrom.indexOf(idEdge) != -1 && filter == idEdge){
                idEdgeExist = idEdge;
              }
          }

	  });
	  return idEdgeExist;
  }

    function existOtherEdge(idFrom, idTo, filter = null){
  	  var idEdgeExist = null;
  	  var edgesFrom = network.getConnectedEdges(parseInt(idFrom));
  	  var edgesTo = network.getConnectedEdges(parseInt(idTo));
  	  var dataPush = [];
  	  edgesTo.forEach(function(idEdge) {

            if(filter == null){
                if(edgesFrom.indexOf(idEdge) != -1)
                  idEdgeExist = idEdge;
            }
            else{
                if(edgesFrom.indexOf(idEdge) != -1 && filter != idEdge){
                  idEdgeExist = idEdge;
                }
            }

  	  });
  	  return idEdgeExist;
    }

  function existEdgeSuper(idFrom, idTo){
	  var idEdgeExist = null;
	  var edgesFrom = network_super.getConnectedEdges(parseInt(idFrom));
	  var edgesTo = network_super.getConnectedEdges(parseInt(idTo));
	  var dataPush = [];
	  edgesTo.forEach(function(idEdge) {
		  if(edgesFrom.indexOf(idEdge) != -1)
			  idEdgeExist = idEdge;
	  });
	  
	  return idEdgeExist;
  }

  function inSuperEntity(idNode){
    var superNod = nodes.get(parseInt(idNode))
    return (superNod.superEntity >= 0);
  }
  
  function existElementName(oneNodeName, typeElement){
	  var exist = false;
	  var i = 0;
	  var allNodes;
	  if(typeElement=="addAttribute"){
		  id_atribute = jQuery('#element').val();
		  id_atribute = parseInt(id_atribute);
		  allNodes = network.getConnectedNodes(id_atribute); 
		  if(oneNodeName == ""){
			  exist = true;
		  }else{
			  
			  while(i<allNodes.length && !exist){
				  if(nodes.get(allNodes[i]).shape != "box"){
					  if(nodes.get(allNodes[i]).label == oneNodeName){
						  exist = true;
					  }
				  }
				  i++
			  }  
		  }
	  }else{
		  allNodes = nodes.getIds({
		  filter: function (item) {
			  return (item.shape == "box" || item.shape == "diamond" || item.shape == "triangleDown");
		  	}
		  });
		  
		  if(oneNodeName == ""){
			  exist = true;
		  }else{
			  
			  while(i<allNodes.length && !exist){
				  if(nodes.get(allNodes[i]).label == oneNodeName){
					  exist = true;
				  }
				  i++
			  }  
		  }
	  }
	  return exist;
  }
  
  function fillEditConstraints(idNodo){
	  idNodo = parseInt(idNodo);
	  valuesConstraints = nodes.get(idNodo).constraints;
	  for(var i=0;i<valuesConstraints.length;i++){
		  if(i!=0){
			  	var nextValue = parseInt($("#totalInputs").val())+1;
		  		var dataType = {
						temp_unique: nextValue,
						temp_value: valuesConstraints[i]
					};
		  		$("#totalInputs").val(nextValue);
				$("#inputList").append($('#templateSelectAddConstraints').tmpl(dataType));
				$('#insertModal').prop('disabled', false);
		  }else{
			  $("#list0").val(valuesConstraints[i]);
		  }
	  }
  }
  
  function fillEditTableUnique(idNodo){
	  idNodo = parseInt(idNodo);
	  valuesUnique = JSON.parse(nodes.get(idNodo).tableUnique);
	  var nodo = allAttributeOfEntity(parseInt($("#idSelected").val()));
	  for(var i=0;i<valuesUnique.length;i++){
		  if(i!=0){
				var nextValue = parseInt($("#totalInputs").val())+1;
		  		var dataType = {
						temp_nodes: nodo,
						temp_unique: nextValue,
						temp_value: ""
					};
		  		$("#totalInputs").val(nextValue);
				$("#inputList").append($('#templateSelectTableUnique').tmpl(dataType));	
		  }
		  for(var e=0;e<valuesUnique[i].length;e++){
				$("#listTextUnique"+i+" option[value='" + valuesUnique[i][e] + "']").prop("selected", true);
		  }
	  }
	  $('.select-multiple').select2();
	  $('#insertModal').prop('disabled', false);
  }
  
  function fillEditRelation(idNodo){
	  idNodo = parseInt(idNodo);  
	  jQuery("#recipient-name").val(nodes.get(idNodo).label);
	  $('#titleModal').html($('#textEditRelation').text());
	  $('#insertModal').prop('disabled', false);
  }
  
  function fillEditEntity(idNodo){
	  idNodo = parseInt(idNodo);
	  jQuery("#recipient-name").val(nodes.get(idNodo).label);
	  $('#titleModal').html($('#textEditEntity').text());
	  $("#weak-entity").prop("checked",nodes.get(idNodo).isWeak);
	  $('#insertModal').prop('disabled', false);
	  $('#weak-entity').change(function(){
		  $('#insertModal').prop('disabled', false);
	  });
  }

  function fillEditSuperEntity(idNodo){
  	  idNodo = parseInt(idNodo);
  	  jQuery("#recipient-name").val(nodes.get(idNodo).label);
  	  $('#titleModal').html($('#textEditSuperEntity').text());
  	  $("#weak-entity").prop("checked",nodes.get(idNodo).isWeak);
  	  $('#insertModal').prop('disabled', false);
  	  $('#weak-entity').change(function(){
  		  $('#insertModal').prop('disabled', false);
  	  });
    }
  
  function existParent(idNodo){
	  var exist = false;
	  var dataFull = network.getConnectedEdges(parseInt(idNodo));
	  
	  dataFull.forEach(function(key){
		  if(edges.get(key).type == "parent")
			  exist = true;
	  });
	  
	  return exist;
  }
  
  /**
   * Obtiene el nodo padre del elemento IsA
   * @param idNodo ELemente IsA
   * @returns
   */
  function getParentId(idNodo){
	  var idParent = -1;
	  var dataFull;
	  if(idNodo != null){
          dataFull = network.getConnectedEdges(parseInt(idNodo));
          dataFull.forEach(function(key){
              if(edges.get(key).type == "parent")
                  idParent = edges.get(key).to;
          });
	  }
	  return idParent;
  }
  
  function getChildData(idNodo){
	  var dataFull;
	  var data = [];
	  if(idNodo != null){
          dataFull = network.getConnectedEdges(parseInt(idNodo));
          dataFull.forEach(function(key){
              if(edges.get(key).type == "child")
                  data.push({id:key, labelChild: nodes.get(edges.get(key).to).label, idChild: nodes.get(edges.get(key).to).id});
          });
	  }
	  return data;
  }
  
  function addEntityChild(idTo, action, idSelected){

	  var data_element = {from: parseInt(idSelected),type:"child", to: parseInt(idTo),arrows:
	  						{from: { enabled: false },middle: { enabled: false },to: { enabled: true }
	  						}
	  					};
	  if(existEdge(idSelected, idTo, null) == null){
		  edges.add(data_element);
          actionHistory.push({ type: 'addEntityToRelation', edge: JSON.parse(JSON.stringify(edges.get(data_element.id))) });
          console.log("[actionHistory] - addEntityToRelation: " + edges.get(data_element.id).from + " - " + edges.get(data_element.id).to);
	  }
	  updateTableElements();
  }
  
  function addSubAttribute(name, action, idSelected, idAttribute = idEntity, comp, notNll, uniq, multi, dom, sz){
	  var word_pk = name;
	  var word_multi = 1;
	  
	  if(!notNll){
		  word_pk +="*";
	  } 
	  if(multi){
		  word_multi = 3;
	  }
	  updateIdCount();
	  var data_element = {id:idCount, labelBackend:name, type: 'subAttribute', borderWidth:word_multi,label: word_pk, dataAttribute:{composite: comp, notNull: notNll, unique: uniq, multivalued: multi, domain: dom, size: sz}, shape: 'ellipse', IsSuperEntity:false, superEntity:-1, color:'#4de4fc', scale:20, widthConstraint:80, heightConstraint:25,physics:false};
	  if(action == "edit"){
		  data_element.id = parseInt(idSelected);
		  data_element.superEntity=nodes.get(data_element.id).superEntity;
		  actionHistory.push({ type: 'modifyNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
		  console.log("[actionHistory] - modify: " + data_element.label);
		  nodes.update(data_element);
	  }else{

           if(poscSelection != null){
      			  data_element.x = poscSelection.x-180;
      			  data_element.y = poscSelection.y+30;
      	   }

            //Añadimos atributo a agregación
           if(inSuperEntity(parseInt(idAttribute))){
                data_element.superEntity = nodes.get(parseInt(idAttribute)).superEntity;
                //Añadimos el nodoV
                nodes.add(data_element);
                nodes_super.add(data_element);
                actionHistory.push({ type: 'addToNewSuperEntity', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
                console.log("[actionHistory] - addToNewSuperEntity: " + data_element.label);
                // Añadimos los edges
                edges.add({from: parseInt(idAttribute), to: parseInt(idCount), color:{color:'#22bdb1'},width: 2});//cambiado
                updateSuperEntityEdges();
                setSuperEntityCoordinates(true, nodes.get(data_element.superEntity));
           }
           // Añadimos atributo fuera de la agregación
           else{
                nodes.add(data_element);
                edges.add({from: parseInt(idAttribute), to: parseInt(idCount), color:{color:'#22bdb1'},width: 2});//cambiado
           }
            actionHistory.push({ type: 'addNode', node: JSON.parse(JSON.stringify(nodes.get(data_element.id))) });
            console.log("[actionHistory] - addNode: " + data_element.label);
            clearUndoneHistory();
            idCount++;

	  }
    updateTableElements();
}
  
  function fillEditAtributte(idNodo){
	  idNodo = parseInt(idNodo);
	  var nameAttribute = nodes.get(idNodo).label;
	  var pk = nameAttribute.split("\n");
	  nameAttribute = pk[0].replace("*","");
	  jQuery("#recipient-name").val(nameAttribute);
	  jQuery("#domain").val(nodes.get(idNodo).dataAttribute.domain);
	  jQuery("#size").val(nodes.get(idNodo).dataAttribute.size);
	  $('#titleModal').html($('#textEditAttribute').text());
	  $("#composite").prop("checked",nodes.get(idNodo).dataAttribute.composite);
	  $("#multivalued").prop("checked",nodes.get(idNodo).dataAttribute.multivalued);
	  $("#notNull").prop("checked",nodes.get(idNodo).dataAttribute.notNull);
	  $("#primaryKey").prop("checked",nodes.get(idNodo).dataAttribute.primaryKey);
	  $("#unique").prop("checked",nodes.get(idNodo).dataAttribute.unique);
	  $('#insertModal').prop('disabled', false);
	  $("label[for='element']" ).hide();
	  $("#element" ).hide();
  }
  
  // Metodo que obtiene el nodo seleccionado con boton derecho y lo almacena en nodoSelect
  network.on('oncontext', function(params) {
	  poscSelect = params.pointer.DOM;
	  poscSelection = params.pointer.canvas;
	  if(typeof network.getNodeAt(poscSelect) !== 'undefined'){
		  nodoSelected = network.getNodeAt(poscSelect);
	  }else{
		  nodoSelected = null;
	  }
	  
	  params.event.preventDefault();
	});

  var drag = false;
  var rect = {}
  var canvas = network.canvas.frame.canvas;
  var ctx = canvas.getContext('2d');
  var drawingSurfaceImageData;
  
  function saveDrawingSurface() {
	   drawingSurfaceImageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
  }
  
  function getStartToEnd(start, theLen) {
	    return theLen > 0 ? {start: start, end: start + theLen} : {start: start + theLen, end: start};
  }
  
  function restoreDrawingSurface() {
	    ctx.putImageData(drawingSurfaceImageData, 0, 0);
  }
  
  //crear boton para poder que dragView: poner a true o false
  
  function selectNodesFromHighlight() {
	    var fromX, toX, fromY, toY;
	    var nodesIdInDrawing = [];
	    var xRange = getStartToEnd(rect.startX, rect.w);
	    var yRange = getStartToEnd(rect.startY, rect.h);

	    var allNodes = nodes.get();
	    for (var i = 0; i < allNodes.length; i++) {

	        var curNode = allNodes[i];
	        var nodePosition = network.getPositions([curNode.id]);
	        var nodeXY = network.canvasToDOM({x: nodePosition[curNode.id].x, y: nodePosition[curNode.id].y});
	        if (xRange.start <= nodeXY.x && nodeXY.x <= xRange.end && yRange.start <= nodeXY.y && nodeXY.y <= yRange.end) {
	            nodesIdInDrawing.push(curNode.id);
	        }
	    }
	    network.selectNodes(nodesIdInDrawing);
  }

  $(document).ready(function() {
	  $(".vis-centrarMover").on("click", function(e){
		  changeDrawView = !changeDrawView;
		  network.setOptions({interaction:{dragView:changeDrawView}});
		  if(changeDrawView){
			  $(".vis-centrarMover").css('background-color', 'transparent');
			  $("#diagram").unbind("mousemove");
			  $("#diagram").unbind("mousedown");
			  $("#diagram").unbind("mouseup");
		  }else{
			  $(".vis-centrarMover").css('background-color', 'rgb(255 0 0 / 27%)');
			  $("#diagram").on("mousemove", function(e) {
			      if (drag) { 
			          restoreDrawingSurface();
			          rect.w = (e.pageX - this.offsetLeft) - rect.startX;
			          rect.h = (e.pageY - this.offsetTop) - rect.startY-80;
			          ctx.setLineDash([5]);
			          var colorRed = '';
			          if(changeDrawView)
			        	  colorRed = 'transparent';
					  else
						  colorRed = 'rgb(255 0 0 / 27%)';
			          ctx.strokeStyle = colorRed;
			          ctx.strokeRect(rect.startX, rect.startY, rect.w, rect.h);
			          ctx.setLineDash([]);
			          ctx.fillStyle = colorRed;
			          ctx.fillRect(rect.startX, rect.startY, rect.w, rect.h);
			      }
			  });
			  $("#diagram").on("mousedown", function(e) {

			      if (e.button == 0) {
			          selectedNodes = e.ctrlKey ? network.getSelectedNodes() : null;
			          saveDrawingSurface();
			          var that = this;
			          rect.startX = e.pageX - this.offsetLeft;
			          rect.startY = e.pageY - this.offsetTop-90;
			          drag = true;
			          if(changeDrawView)
			        	  container.style.cursor = "default";
			          else
			        	  container.style.cursor = "crosshair";
			          if(nodes_selected_event){
			        	  $("#diagram").unbind("mousemove");
			        	  container.style.cursor = "default";
			          }else{
			        	  $("#diagram").bind("mousemove", function(e) {
						      if (drag) { 
						          restoreDrawingSurface();
						          rect.w = (e.pageX - this.offsetLeft) - rect.startX;
						          rect.h = (e.pageY - this.offsetTop) - rect.startY-80;
						          ctx.setLineDash([5]);
						          var colorRed = '';
						          if(changeDrawView)
						        	  colorRed = 'transparent';
								  else
									  colorRed = 'rgb(255 0 0 / 27%)';
						          ctx.strokeStyle = colorRed;
						          ctx.strokeRect(rect.startX, rect.startY, rect.w, rect.h);
						          ctx.setLineDash([]);
						          ctx.fillStyle = colorRed;
						          ctx.fillRect(rect.startX, rect.startY, rect.w, rect.h);
						      }
						  });
			          }
			      }
			  });
			  $("#diagram").on("mouseup", function(e) {
			      if (e.button == 0) {
			          restoreDrawingSurface();
			          drag = false;
			          container.style.cursor = "default";
			          selectNodesFromHighlight();
			          if(network.getSelectedNodes().length>0)
			        	  nodes_selected_event = true;
			          else
			        	  nodes_selected_event = false;
			      }
			  });
		  }
	  });
	  /*

	  */
  });
  
  function getNodeSelected(){
	  return nodoSelected;
  }
 
  function setNodeSelected(value){
	  nodoSelected = value;
  }
 
  function existDataTableUnique(idSelected){
	  idSelected = parseInt(idSelected);
	  return (nodes.get(idSelected).tableUnique === undefined)
  }
  
  function getIsSubAttribute(idSelected){
	  idSelected = parseInt(idSelected);
	  return (nodes.get(idSelected).type == "subAttribute")
  }
  
  /**
   * 
   * @param id de un nodo tipo atributo
   * @returns Devuelve true si es un atributo compuesto o no
   */
  function getComposedEllipse(nodo_select){
	  var idNodo = parseInt(nodo_select);
	  return (nodes.get(idNodo).dataAttribute.composite)
  }
  
  
  function existConstraints(idSelected){
	  idSelected = parseInt(idSelected);
	  return (nodes.get(idSelected).constraints === undefined)
  }
  
  function allEntitysToRelation2(nodo_select, onlyType=null,  entityId=null){
	  var data = [];
	  var dataAll = [];
	  var type = "all";
	  
	  if(onlyType != null){
		  type = onlyType;
	  }

	  nodos = network.getConnectedEdges(parseInt(nodo_select));
	  nodos.forEach(function(edg) {
		  	idNodo = edges.get(edg).to;
		  	roleName = edges.get(edg).label;
		  	labelF = edges.get(edg).labelFrom;
		  	labelT = edges.get(edg).labelTo;


            if(entityId !=null){
                if(nodes.get(idNodo).shape == "box" && idNodo == entityId){
                    dataAll.push({id:edg, label:nodes.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});

                }
            }
            else{
                if(nodes.get(idNodo).shape == "box"){
                    dataAll.push({id:edg, label:nodes.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});

                }else
                    dataAll.push({id:edg, label:nodes.get(idNodo).label, role:roleName});

            }
        });
        return dataAll;

	  
  }
  /**
   * Devuelve los elementos de una relacion, todas o solo las del tipo especificado
   * @param nodo_select id del elemento tipo relacion del que se quiere obtener sus elementos conectados
   * @param onlyType si es distinto de null filtra los elementos que se quiere obtener
   * @returns Devuelve un array con los datos
   */
  function allEntitysToRelation(nodo_select, onlyType=null){
	  var data = [];
	  var dataAll = [];
	  var type = "all";
	  
	  if(onlyType != null){
		  type = onlyType;
	  }

	  nodos = network.getConnectedEdges(parseInt(nodo_select));
	  nodos.forEach(function(edg) {
		  	idNodo = edges.get(edg).to;
		  	roleName = edges.get(edg).label;
		  	labelF = edges.get(edg).labelFrom;
		  	labelT = edges.get(edg).labelTo;
		  	if(nodes.get(idNodo).shape == type){
		  		if(nodes.get(idNodo).shape == "box")
		  			data.push({id:edg, label:nodes.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});
		  		else
		  			data.push({id:edg, label:nodes.get(idNodo).label, role:roleName});
		  	}
		  	if(nodes.get(idNodo).shape == "box")
		  		dataAll.push({id:edg, label:nodes.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});
	  		else
	  			dataAll.push({id:edg, label:nodes.get(idNodo).label, role:roleName});
		  		
	  });
	  
	  if(onlyType != null){
		  return data;
	  }else{
		  return dataAll;
	  }
	  
  }
  
  function allEntitysToRelationSuper(nodo_select, onlyType=null){
	  var data = [];
	  var dataAll = [];
	  var type = "all";
	  
	  if(onlyType != null){
		  type = onlyType;
	  }

	  nodos = network_super.getConnectedEdges(parseInt(nodo_select));
	  nodos.forEach(function(edg) {
		  	idNodo = edges_super.get(edg).to;
		  	roleName = edges_super.get(edg).label;
		  	labelF = edges_super.get(edg).labelFrom;
		  	labelT = edges_super.get(edg).labelTo;
		  	if(nodes_super.get(idNodo).shape == type){
		  		if(nodes_super.get(idNodo).shape == "box")
		  			data.push({id:edg, label:nodes_super.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});
		  		else
		  			data.push({id:edg, label:nodes_super.get(idNodo).label, role:roleName});
		  	}
		  	if(nodes_super.get(idNodo).shape == "box")
		  		dataAll.push({id:edg, label:nodes_super.get(idNodo).label, role:roleName, asoc:labelF+"-"+labelT});
	  		else
	  			dataAll.push({id:edg, label:nodes_super.get(idNodo).label, role:roleName});
		  		
	  });
	  
	  if(onlyType != null){
		  return data;
	  }else{
		  return dataAll;
	  }
	  
  }

  function allAttributeOfEntity(nodo_select){

	  var data = [];
	  if(nodes.get(nodo_select).shape !== 'ellipse'){
          var nodos = network.getConnectedEdges(parseInt(nodo_select));
          nodos.forEach(function(edg) {
              var aux = nodes.get(nodo_select);

                idNodo = edges.get(edg).to;
                roleName = edges.get(edg).label;
                if(nodes.get(idNodo).shape == "ellipse"){
                    data.push({id:idNodo, label:nodes.get(idNodo).labelBackend, type:nodes.get(idNodo).dataAttribute.domain, size:nodes.get(idNodo).dataAttribute.size});
          }});
	  }
	  return data;
  }

    function allSubAttribute(nodo_select){
      var data = [];
      var nodos = network.getConnectedEdges(parseInt(nodo_select));
      nodos.forEach(function(edg) {
            var aux = nodes.get(nodo_select);

            idNodo = edges.get(edg).to;
            roleName = edges.get(edg).label;
            if(nodes.get(idNodo).type == "subAttribute"){
                data.push({id:idNodo, label:nodes.get(idNodo).labelBackend, type:"subAttribute", size:nodes.get(idNodo).dataAttribute.size});
      }});
      return data;
    }
  
  function allAttributeOfEntitySuper(nodo_select){
	  var data = [];
	  nodos = network_super.getConnectedEdges(parseInt(nodo_select));
	  nodos.forEach(function(edg) {
		  	idNodo = edges_super.get(edg).to;
		  	roleName = edges_super.get(edg).label;
		  	if(nodes_super.get(idNodo).shape == "ellipse")
		  		data.push({id:idNodo, label:nodes_super.get(idNodo).labelBackend, type:nodes_super.get(idNodo).dataAttribute.domain, size:nodes_super.get(idNodo).dataAttribute.size});				  
	  });
	  return data;
  }
  
  function allEntityOfRelation(nodo_select){
	  var data = [];
	  nodos = network.getConnectedEdges(parseInt(nodo_select));
	  nodos.forEach(function(edg) {
		  	idNodo = edges.get(edg).to;
		  	roleName = edges.get(edg).label;
		  	if(nodes.get(idNodo).shape == "box")
		  		data.push(nodes.get(idNodo));
	  });
	  return data;
  }

    function allUniqueEntityOfRelation(nodo_select){
  	  var data = [];
  	  var dataAux = [];
  	  nodos = network.getConnectedEdges(parseInt(nodo_select));
  	  nodos.forEach(function(edg) {
  		  	idNodo = edges.get(edg).to;
  		  	roleName = edges.get(edg).label;
  		  	if(nodes.get(idNodo).shape == "box" && dataAux.indexOf(idNodo) == -1){
  		  	    data.push(nodes.get(idNodo));
  		  	    dataAux.push(idNodo);
  		  	}

  	  });
  	  return data;
    }
  
  /* domains**/
  
  function getAllTypesDomain(){
	  return typeDomain.getTypesDomains();
  }
  
  function addTypeDomain(nameType, type, values_separated, typeAction){
	  var id = nameType.replace(/ /g, "_");
	  typeDomain.setTypesDomains(id.toLowerCase(), nameType, type, values_separated);
  }
  
  function getTypeItem(idItem){
	  return nodes.get(parseInt(idItem)).shape;
  }
  
  function getNodesSelectedCount(){
	  return network.getSelectedNodes().length;
  }
  
  function deleteNodeSelected(id = null){

	if(id==null){
		var dat = network.getSelectedNodes();
	}else{
		var dat = [parseInt(id)];
	}
	
	var attr = allAttributeOfEntity(getNodeSelected());
	var attrsId = [];
	var idSuperEntity;

	if(nodes.get(dat[0]).IsSuperEntity){
	    deleteSuperEntity(dat);
	}
	else{
        actionHistory.push({ type: 'startDelete', node: null});
        console.log("[actionHistory] - startDelete ");

        dat.forEach(function(id) {
            var nod = nodes.get(id);
            actionHistory.push({ type: 'deleteNode', node: JSON.parse(JSON.stringify(nod)) });
            console.log("[actionHistory] - deleteNode: " + nod.label);
            if(nod.superEntity >=0) {
                nodes_super.remove(nod.id);
                idSuperEntity = nod.superEntity;
            }


            // Borramos los ATRIBUTOS conectados

            var attr = allAttributeOfEntity(id);

            attr.forEach(function(elem) {
                // Borramos el atributo si este no se encuentra seleccionado
                if(!dat.includes(elem.id)){

                    actionHistory.push({ type: 'deleteNode', node: JSON.parse(JSON.stringify(nodes.get(elem.id))) });
                    console.log("[actionHistory] - deleteNode: " + nodes.get(elem.id).label);
                    attrsId.push(elem.id);

                    if(elem.superEntity >=0) nodes_super.remove(elem.id);
                }

            });

            attrsId.push(id);

            // Borramos los EDGES conectados

            var connectedEdges = network.getConnectedEdges(id);

            connectedEdges.forEach(function(edg) {

                actionHistory.push({ type: 'deleteEdge', edge: JSON.parse(JSON.stringify(edges.get(edg))) });
                console.log("[actionHistory] - deleteEdge: " + nodes.get(edges.get(edg).from).label + " - " + nodes.get(edges.get(edg).to).label);
                edges.remove(edg.id);

                if(inSuperEntity(edges.get(edg).from)&&inSuperEntity(edges.get(edg).to)) edges_super.remove(edg);

            });

        });

        actionHistory.push({ type: 'stopDelete', node: null});
        console.log("[actionHistory] - stopDelete ");

	}
	
	network.selectNodes(attrsId);
	network.deleteSelected();
	if(idSuperEntity >=0)setSuperEntityCoordinates(true, nodes.get((idSuperEntity)));
	updateTableElements();
  }
  
  function printDomains(){
	  typeDomain.print("#itemsDomains");
  }

  function getSuperEntityNode(superEntityId) {
    var r = null;
    var allNodes = nodes.get();

    allNodes.forEach(function(nodeId){
        if(superEntityId !=null && nodeId.IsSuperEntity) r = nodeId;

    });

    return r;
  }

  function getAllSuperEntityNodes(){
    var sNodes = [];
    var allNodes = nodes.get();
    allNodes.forEach(function(nod){
        if(nod.IsSuperEntity) sNodes.push(nod);
    });

    return sNodes;
  }

 network.on('dragStart', function (params) {

    var selectedNodes = network.getSelectedNodes();
    var nodesToSelect= selectedNodes;
    var superEntitySelected = [];
    selectedNodes.forEach(function(nodeId) {
        var nod = nodes.get(nodeId);
        if(nod.IsSuperEntity) superEntitySelected.push(nod);

    });

    superEntitySelected.forEach(function(nod) {
        nodes.forEach(function(node) {
            if (node.superEntity === nod.id && !selectedNodes.includes(node.id)){
              nodesToSelect.push(node.id);
            }
        });
    });
    network.selectNodes(nodesToSelect);
});

  network.on('dragEnd', function (params) {
        var i=0;
        var superEntityId = [];
        if (params.nodes.length > 0) {
            actionHistory.push({ type: 'startMovingNode', node: null });

            while(i < params.nodes.length){
                // Obtenemos el nodo movido
                var nodeId = params.nodes[i];
                var movedNode = nodes.get(nodeId);
                // Obtenemos la posición del nodo que se ha movido
                var movedNodePos = network.getPosition(movedNode.id);

                // Actualizamos el nodo con la nueva posición
                nodes.update({id: movedNode.id, x: movedNodePos.x, y: movedNodePos.y});
                actionHistory.push({ type: 'moveNode', node: JSON.parse(JSON.stringify(movedNode)) });

                if(movedNode.superEntity >=0){
                    superEntityId.push(movedNode.superEntity);
                    nodes_super.update({id: movedNode.id, x: movedNodePos.x, y: movedNodePos.y});
                }
                i++;

            }

            superEntityId.forEach(function(sNode) {
                var superNode = nodes.get(sNode);
                if(superNode!=null) {
                    setSuperEntityCoordinates(true, superNode);
                }
            });
                actionHistory.push({ type: 'stopMovingNode', node: null });
        }
    });


$(document).ready(function() {
    $(".changeOptions").on("click", function(event) {

        var theme = $(this).attr('href').split('=')[1];
        //Actualizamos el color de fuente de la agregación
        var superNode = getAllSuperEntityNodes();
        superNode.forEach(function(sNode) {
            sNode.font.color = (theme === 'light') ? '#000000' : '#ffffff';
            nodes.update([sNode]);
        });

    });
});




function undoLastAction() {
    if (actionHistory.length === 0) {
        console.log("No hay acciones para deshacer.");
        console.log(nodes.length);
        alert($('#textNoUndoActions').text());
        return;
    }

    var lastAction = actionHistory.pop();
    console.log("Deshaciendo acción:", lastAction);
    console.log(actionHistory);

    // Añadimos al buffer de rehacer la acción a deshacer
    undoneHistory.push(lastAction);
    console.log("Metemos en Y: ", lastAction);
    console.log(undoneHistory);

    //  NODES ACTIONS

    if (lastAction.type === 'deleteNode') {
        // Restaurar el nodo al estado anterior
        nodes.update(lastAction.node);

        var nextAction = actionHistory[actionHistory.length - 1];

        if(nextAction.type !== 'startDelete'){

            undoLastAction();
        }
        else{
            actionHistory.pop();
            undoneHistory.push(nextAction);
        }

        if(lastAction.node.superEntity >=0) {
            nodes_super.update(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }

    }
    else if (lastAction.type === 'addNode') {
        // Restaurar el nodo al estado anterior
        nodes.remove(lastAction.node);
        if(lastAction.node.superEntity >=0) {
            nodes_super.remove(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }

    }
    else if (lastAction.type === 'modifyNode') {
        // Restaurar el nodo al estado anterior
        var aux_node = nodes.get(lastAction.node.id);
        console.log(" modify node: " + lastAction.node.label + " - " + nodes.get(lastAction.node.id).label);

        nodes.update(lastAction.node);
        if(lastAction.node.superEntity >= 0) {
            nodes_super.update(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }
        undoneHistory[undoneHistory.length - 1].node = aux_node;
    }
    else if (lastAction.type === 'stopDelete'){
        undoLastAction();
    }
    else if(lastAction.type === 'stopMovingNode'){
        var nextAction = actionHistory[actionHistory.length - 1];
        while(nextAction.type !== 'startMovingNode' && actionHistory.length > 0){
            undoLastAction();
            nextAction = actionHistory[actionHistory.length - 1];
        }
        undoLastAction();
    }
    else if(lastAction.type === 'moveNode'){
        var x = nodes.get(lastAction.node.id).x;
        var y = nodes.get(lastAction.node.id).y;
        nodes.update(lastAction.node);
        undoneHistory[undoneHistory.length-1].node.x = x;
        undoneHistory[undoneHistory.length-1].node.y = y;

        if(lastAction.node.superEntity >= 0)
            nodes_super.update(lastAction.node);
    }

    //  ENTITY ACTIONS

    else if(lastAction.type === 'stopWeakEntity'){
        console.log(" stopWeakEntity ");
        var nextAction = actionHistory[actionHistory.length - 1];
        while(nextAction.type !== 'startWeakEntity' && actionHistory.length > 0){
            undoLastAction();
            nextAction = actionHistory[actionHistory.length - 1];
        }
        undoLastAction();

    }

    //  SUPER ENTITY ACTIONS

    else if (lastAction.type === 'deleteSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteSuperEntity " + lastAction.node.label);
        nodes.update(lastAction.node);
        undoLastAction();

    }
    else if (lastAction.type === 'addSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" addSuperEntity: " + lastAction.node.label);

        var cont = actionHistory.length-1;
        while(cont>=0 && actionHistory[cont].type !== 'startAddToNewSuperEntity'){
            undoLastAction();
            cont--;
        }
        undoLastAction();
        nodes.remove(lastAction.node);

    }
    else if (lastAction.type === 'deleteFromSuperEntity' || lastAction.type === 'deleteWithSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteFromSuperEntity " + lastAction.node.label);
        nodes.update(lastAction.node);
        nodes_super.update(lastAction.node);
        setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        undoLastAction();

    }
    else if (lastAction.type === 'stopAddToNewSuperEntity') {
        console.log(" stopAddToNewSuperEntity: ");

        var cont = actionHistory.length-1;
        while(cont>=0 && actionHistory[cont].type !== 'startAddToNewSuperEntity'){
            // Estas acciones se dan cuando no se ha podido crear la agregación, las borramos de los array de Z y de Y
            undoneHistory.pop();
            undoLastAction();
            cont--;
        }
        undoLastAction();
        undoneHistory.pop();

    }
    else if (lastAction.type === 'addToNewSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" addToNewSuperEntity " + lastAction.node.label + " " + lastAction.node.superEntity);
        //if(lastAction.node.shape != "ellipse"){
            nodes.update({id: lastAction.node.id, superEntity: -1});
            if(lastAction.node.superEntity >= 0) {
                nodes_super.remove(lastAction.node);
            }
        //}
        /*else {
            nodes.remove(lastAction.node)
            nodes_super.remove(lastAction.node);
            setSuperEntityCoordinates(true,nodes.get(lastAction.node.superEntity));
        }*/

    }
    else if (lastAction.type === 'addToSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" addToSuperEntity " + lastAction.node.label);
        nodes.remove(lastAction.node);
        if(lastAction.node.superEntity >= 0) {
            nodes_super.remove(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }

    }
    else if (lastAction.type === 'stopAddToSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" stopAddToSuperEntity ");
        var cont = actionHistory.length-1;
        var nextAction;
        while(cont>=0 && actionHistory[cont].type === 'addToSuperEntity'){
            console.log("Deshaciendo acción:", actionHistory[cont]);

            console.log("actionHistory[cont]: " + actionHistory[cont].node);
            nextAction = actionHistory.pop();
            console.log("nextAction: " + nextAction.node);
            nodes.update(nextAction.node);
            nodes_super.remove(nextAction.node);
            // Actualizamos el valor de la super entidad
            nextAction.node.superEntity = lastAction.node;
            undoneHistory.push(nextAction);

            cont--;
        }

        setSuperEntityCoordinates(true, nodes.get(lastAction.node));

        if(cont>=0 && actionHistory[cont].type === 'addNewEntityToRelation') undoLastAction();

    }
    else if (lastAction.type === "stopSuperEntityDelete"){
        undoLastAction();
    }

    //  RELATION ACTIONS

    else if (lastAction.type === 'deleteEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteEntityToRelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.update(lastAction.edge);
        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.update(lastAction.edge);
        }
    }
    else if (lastAction.type === 'addNewEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" addNewEntitytoRelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.remove(lastAction.edge);
        var idExist = existEdge(lastAction.edge.from, lastAction.edge.to, null);
        if(idExist != null){
            var data_element_update = {};
            data_element_update.id = idExist;
            data_element_update.state = "false";
            edges.update(data_element_update);

        }
        while(actionHistory[actionHistory.length-1].type === 'addEntitytoRelation'){
            var nextAction = actionHistory.pop();
            console.log(" nextAction: " + nextAction.edge.from + " - " + nextAction.edge.to);
            edges.update(nextAction.edge);
            if(getSuperEntityNode !=null && inSuperEntity(nextAction.edge.to) && inSuperEntity(nextAction.edge.from)){
                edges_super.remove(nextAction.edge);
            }

            undoneHistory.push(lastAction);
        }
        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.remove(lastAction.edge);
        }
    }
    else if (lastAction.type === 'addEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" addEntityToRelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.remove(lastAction.edge);
        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.remove(lastAction.edge);
        }
    }
    else if (lastAction.type === 'modifyEntityToRelation' || lastAction.type === 'modifyOtherEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" modifyEntitytoRelation " + lastAction.edge.from + " - " + lastAction.edge.to);

        var aux_edge = edges.get(lastAction.edge.id);
        edges.update(lastAction.edge);
        undoneHistory[undoneHistory.length - 1].edge = aux_edge;

        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.update(lastAction.edge);
        }
        if(lastAction.type === 'modifyOtherEntityToRelation') undoLastAction();

    }

    // ISA ACTIONS

    else if (lastAction.type === 'deleteIsARelation') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteIsARelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.update(lastAction.edge);

        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.update(lastAction.edge);
        }

    }
    else if (lastAction.type === 'startDeleteIsA'){

        var cont = actionHistory.length - 1;
        var nextAction = actionHistory[cont];
        while(cont > 0 && nextAction.type !== 'stopDeleteIsA'){
            undoLastAction();
            nextAction = actionHistory[--cont];
        }
        actionHistory.pop();
        undoneHistory.push(nextAction);
    }

    // EDGES ACTIONS

    else if (lastAction.type === 'deleteEdge'){
        edges.update(lastAction.edge);

        var nextAction = actionHistory[actionHistory.length - 1];
        if(nextAction.type !== 'startDelete') undoLastAction();
        else{
            actionHistory.pop();
            undoneHistory.push(nextAction);
        }
    }

    updateTableElements();
}

function redoLastAction() {
    if (undoneHistory.length === 0) {
        console.log("No hay acciones para rehacer.");
        console.log(nodes.length);
        alert($('#textNoRedoActions').text());
        return;
    }

    var lastAction = undoneHistory.pop();
    console.log("Rehaciendo acción:", lastAction);
    console.log(undoneHistory);

    actionHistory.push(lastAction);

    //  NODES ACTIONS

    if (lastAction.type === 'deleteNode') {
        // Restaurar el nodo al estado anterior
        nodes.remove(lastAction.node);

        var nextAction = undoneHistory[undoneHistory.length - 1];
        if(nextAction.type !== 'stopDelete'){
            redoLastAction();
        }
        else{
            undoneHistory.pop();
            actionHistory.push(nextAction);
        }

        if(lastAction.node.superEntity >= 0) {
            nodes_super.remove(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }

    }
    else if (lastAction.type === 'addNode') {
        // Restaurar el nodo al estado anterior
        nodes.update(lastAction.node);

        if(lastAction.node.superEntity >= 0) {
            nodes_super.update(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }

    }
    else if (lastAction.type === 'modifyNode') {
        // Restaurar el nodo al estado anterior
        console.log(" modify node: " + lastAction.node.label);
        var aux_node = nodes.get(lastAction.node.id);

        nodes.update(lastAction.node);
        if(lastAction.node.superEntity >= 0) {
            nodes_super.update(lastAction.node);
            setSuperEntityCoordinates(true, nodes.get(lastAction.node.superEntity));
        }
        actionHistory[actionHistory.length - 1].node = aux_node;
    }
    else if(lastAction.type === 'startDelete'){
        redoLastAction();
    }
    else if(lastAction.type === 'startMovingNode'){
        var nextAction = undoneHistory[undoneHistory.length - 1];
        while(nextAction.type !== 'stopMovingNode' && undoneHistory.length > 0){
            redoLastAction();
            nextAction = undoneHistory[undoneHistory.length - 1];
        }

        redoLastAction();
    }
    else if(lastAction.type === 'moveNode'){
        console.log("Y: move node from x: " +nodes.get(lastAction.node.id).x + " - to: " + lastAction.node.x);
        var x = nodes.get(lastAction.node.id).x;
        var y = nodes.get(lastAction.node.id).y;
        nodes.update(lastAction.node);
        actionHistory[actionHistory.length-1].node.x = x;
        actionHistory[actionHistory.length-1].node.y = y;
        if(lastAction.node.superEntity >= 0)
            nodes_super.update(lastAction.node);
    }

    //  ENTITY ACTIONS

    else if(lastAction.type === 'startWeakEntity'){
        console.log(" startWeakEntity ");
        var nextAction = undoneHistory[undoneHistory.length - 1];
        while(nextAction.type !== 'stopWeakEntity' && undoneHistory.length > 0){
            redoLastAction();
            nextAction = undoneHistory[undoneHistory.length - 1];
        }

        redoLastAction();
    }

    //  SUPER ENTITY ACTIONS

    else if (lastAction.type === 'deleteSuperEntity') {    //TODO: test
        // Restaurar el nodo al estado anterior
        console.log(" deleteSuperEntity " + lastAction.node.label);
        nodes.remove(lastAction.node);
    }
    else if (lastAction.type === 'addSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" addSuperEntity: " + lastAction.node.label);
        nodes.update(lastAction.node);
        setSuperEntityCoordinates(true, nodes.get(lastAction.node.id));
    }
    else if (lastAction.type === 'deleteFromSuperEntity') {    //TODO: test
        // Restaurar el nodo al estado anterior
        console.log(" deleteFromSuperEntity " + lastAction.node.label);
        nodes.update({id: lastAction.node.id, superEntity: -1})
        nodes_super.remove(lastAction.node);

        redoLastAction();
    }
    else if (lastAction.type === 'deleteWithSuperEntity') {    //TODO: test
        // Restaurar el nodo al estado anterior
        console.log(" deleteFromSuperEntity " + lastAction.node.label);
        nodes.remove(lastAction.node);
        nodes_super.remove(lastAction.node);

        redoLastAction();
    }
    else if (lastAction.type === 'addToNewSuperEntity') {
        // Restaurar el nodo al estado anterior
        console.log(" addToNewSuperEntity " + lastAction.node.label);

        nodes.update(lastAction.node);
        if(lastAction.node.superEntity >= 0) {
            nodes_super.update(lastAction.node);
            console.log("¡¡ "+lastAction.node.superEntity);
            //if(lastAction.node.shape =="ellipse")
              // setSuperEntityCoordinates(true, getSuperEntityNode(lastAction.node.superEntity));
        }
    }
    else if (lastAction.type === 'startAddToNewSuperEntity'){

        // Hay más nodos que modificar
        var cont = undoneHistory.length-1;
        while(cont>=0 && undoneHistory[cont].type !== 'addSuperEntity'){
            redoLastAction();
            cont--;
        }
        redoLastAction();
    }
    else if (lastAction.type === 'addToSuperEntity'){
        console.log("addtosuperentity superEntity: " + lastAction.node.superEntity);
        nodes.update({id: lastAction.node.id, superEntity: lastAction.node.superEntity});
        nodes_super.update(nodes.get(lastAction.id));

        // Hay más nodos que modificar
        if(undoneHistory.length > 0 && (undoneHistory[undoneHistory.length-1].type === 'addToSuperEntity' || undoneHistory[undoneHistory.length-1].type === 'stopAddToSuperEntity')) redoLastAction();
    }
    else if (lastAction.type === 'stopAddToSuperEntity') {
        setSuperEntityCoordinates(true, nodes.get(lastAction.node));
    }
    else if (lastAction.type === 'startSuperEntityDelete'){
        redoLastAction();
    }
    //  RELATION ACTIONS

    else if (lastAction.type === 'deleteEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteEntityToRelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.remove(lastAction.edge);
        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.remove(lastAction.edge);
        }
        if(existEdge(lastAction.edge.from, lastAction.edge.to, null)) redoLastAction();

    }
    else if (lastAction.type === 'addNewEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" addNewEntitytoRelation " + lastAction.edge.from + " - " + lastAction.edge.to);

        var idExist = existEdge(lastAction.edge.from, lastAction.edge.to, null);
        if(idExist != null){
            var data_element_update = {};
            data_element_update.id = idExist;
            data_element_update.state = (lastAction.edge.status === "left") ? "left" : "right";
            edges.update(data_element_update);

        }
        edges.update(lastAction.edge);

        if(undoneHistory.length > 0 && (undoneHistory[undoneHistory.length-1].type === 'addToSuperEntity' || undoneHistory[undoneHistory.length-1].type === 'stopAddToSuperEntity')) redoLastAction();

        else{
            while(undoneHistory.length > 0 && undoneHistory[undoneHistory.length-1].type === 'addEntitytoRelation'){
                var nextAction = undoneHistory.pop();
                console.log(" nextAction: " + nextAction.edge.from + " - " + nextAction.edge.to);
                edges.update(nextAction.edge);
                if(getSuperEntityNode !=null && inSuperEntity(nextAction.edge.to) && inSuperEntity(nextAction.edge.from)){
                    edges_super.update(nextAction.edge);
                }
            }
            if(getSuperEntityNode() !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
                edges_super.update(lastAction.edge);
            }
        }
    }
    else if (lastAction.type === 'addEntityToRelation') {    //TODO: test
        // Restaurar el nodo al estado anterior
        console.log(" addEntityToRelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.update(lastAction.edge);
        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.add(lastAction.edge);
        }
    }
    else if (lastAction.type === 'modifyEntityToRelation' || lastAction.type === 'modifyOtherEntityToRelation') {
        // Restaurar el nodo al estado anterior
        console.log(" modifyEntitytoRelation " + lastAction.edge.from + " - " + lastAction.edge.to);

        var aux_edge = edges.get(lastAction.edge.id);
        edges.update(lastAction.edge);
        actionHistory[actionHistory.length - 1].edge = aux_edge;

        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.update(lastAction.edge);
        }
    }

    // ISA ACTIONS

    else if (lastAction.type === 'deleteIsARelation') {
        // Restaurar el nodo al estado anterior
        console.log(" deleteIsARelation " + lastAction.edge.from + " - " + lastAction.edge.to);
        edges.remove(lastAction.edge);

        if(getSuperEntityNode !=null && inSuperEntity(lastAction.edge.to) && inSuperEntity(lastAction.edge.from)){
            edges_super.update(lastAction.edge);
        }
    }
    else if(lastAction.type === 'stopDeleteIsA'){
        var cont = undoneHistory.length - 1;
        var nextAction = undoneHistory[cont];
        while(cont > 0 && nextAction.type !== 'startDeleteIsA'){
            redoLastAction();
            nextAction = undoneHistory[--cont];
        }
        undoneHistory.pop();
        actionHistory.push(nextAction);
    }

    // EDGES ACTIONS

    else if (lastAction.type === 'deleteEdge'){
        edges.remove(lastAction.edge);

        var nextAction = undoneHistory[undoneHistory.length - 1];
        if(nextAction.type !== 'startDelete') redoLastAction();
        else {
            undoneHistory.pop();
            actionHistory.push(nextAction);
        }
    }
    updateTableElements();
}

function clearUndoneHistory(){
    console.log("Borramos UndoneHistory: "+ undoneHistory.length);
    undoneHistory.splice(0, undoneHistory.length);
    console.log("Tras borrado de UndoneHistory: "+ undoneHistory.length);
}

document.addEventListener('keydown',function(event) {
    if(event.ctrlKey && event.key.toLowerCase() === 'z'){ // ctrl + z
        console.log(" CTRL + Z");
        undoLastAction();
    }

    else if(event.ctrlKey && event.key.toLowerCase() === 'y'){ // ctrl + y
        console.log(" CTRL + Y");
        redoLastAction();
    }
});



