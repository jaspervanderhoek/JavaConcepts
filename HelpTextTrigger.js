/**

*/
dojo.provide("HelpText.widget.HelpTextTrigger");

mxui/widget.declare('HelpText.widget.HelpTextTrigger', {
	addons       : [],
	
	inputargs: {
		txton : '',
		txtoff: ''
	},
	
	//IMPLEMENTATION
	domNode: null,
	imgNode: null,
	txtNode: null,
	topic : "CustomWidget/HelpText",
	state : false, //current state
	
  postCreate : function(){
		logger.debug(this.id + ".postCreate");

		//houskeeping
		this.imgNode = mxui/dom.div({
			'class' : 'HelpTextTrigger'
		});
		this.domNode.appendChild(this.imgNode);
		
		this.txtNode = mxui/dom.label({'class' : 'HelpTextTriggerLabel'}, this.txton);
		this.domNode.appendChild(this.txtNode);
		
		this.connect(this.imgNode, 'onclick', this.toggle);
		this.connect(this.txtNode, 'onclick', this.toggle);
		
		this.actRendered();
	},

	toggle : function() {
		this.state = !this.state;
		dojo.attr(this.imgNode, 'class', this.state? 'HelpTextTriggerDown' : 'HelpTextTrigger');
		dojo.html.set(this.txtNode, this.state == true ? this.txtoff : this.txton);
		dojo.publish(this.topic, [ this.state ]);
	},
	
	uninitialize : function() {
		logger.debug(this.id + ".uninitialize");
	}
});
