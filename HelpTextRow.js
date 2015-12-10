dojo.provide("HelpText.widget.HelpTextRow");

mxui/widget.declare('HelpText.widget.HelpTextRow', {
	addons       : [],
	
	inputargs: {
		text : '',
		startvisible : false,
		height : 300,
		hideonclick : false,
		onclickmf : ''
	},
	
	//IMPLEMENTATION
	domNode: null,
	topic : "CustomWidget/HelpText",
	handle : null,
	rowNode : null,
	targetHeight : 0,
	anim : null,
	contextobj : null,
	
	postCreate : function(){
		logger.debug(this.id + ".postCreate");

		dojo.addClass(this.domNode, 'HelpTextRow');
		this.createHelp();
		this.rowNode = this.findRowNode(this.domNode);
		dojo.style(this.domNode, 'maxHeight', this.height + 'px');
		dojo.style(this.rowNode, 'height', 'auto'); //follow the animation
		this.actRendered();
		this.addOnLoad(dojo.hitch(this, this.poststartup));
	},

	update : function (obj, callback) {
		this.contextobj = obj;
		callback && callback();
	},
	
	poststartup : function() {
		if (!this.startvisible) {
			dojo.style(this.rowNode, 'display','none');
		}
			
		this.stateChange(this.startvisible);
		this.handle = dojo.subscribe(this.topic, this, this.stateChange);
			
	},
	
	findRowNode : function(parent) {
		var tag = parent.tagName.toLowerCase();
		if (tag == 'tr' || tag == 'th')
			return parent;
		else if (parent.parentNode != null)
			return this.findRowNode(parent.parentNode);
		throw new Exception(this.id + " Did not found a parent row to show or hide");
	},

	updateHeight : function(height) {
		if (this.anim != null)
			this.anim.stop();
		this.anim = dojo.animateProperty({
			node : this.domNode,
			duration : 500,
			properties : { height : height },
			onEnd : dojo.hitch(this, function() {
				if (height == 0)
					dojo.style(this.rowNode, 'display', 'none');
			})
		});
		this.anim.play();
	},

	stateChange : function(newstate) {
		if (newstate) {
			var boxorig = dojo.marginBox(this.domNode);
            dojo.style(this.rowNode, { 'display' : '' });
            dojo.style(this.domNode, {'height' : 'auto'});
            var box = dojo.marginBox(this.domNode);
            
            if (boxorig.h == 0) //restart animation
                dojo.style(this.domNode,  { 'height' : '0px'});
			if (box.h > 0)
                this.updateHeight(Math.min(this.height, box.h));
            else
                dojo.style(this.domNode,  'height', 'auto');
		}
		else { 
			this.updateHeight(0);
		}
	},
	
	createHelp : function () {
		dojo.html.set(this.domNode, this.text);
		if (this.hideonclick === true)
			this.connect(this.domNode, 'onclick', this.hideHelp);
		else if (this.onclickmf != '') {
			this.connect(this.domNode, 'onclick', this.executeMF);
		}
	},

	executeMF : function () {
		mx.data.action({
			error       : function() {
				logger.error(this.id + "error: XAS error executing microflow");
			},
			callback    : dojo.hitch(this, function() {
			}),
			actionname  : this.onclickmf,
			applyto     : 'selection',
			guids       : [mendix/lib/MxObject#getGuid()]
		});
	},

	hideHelp : function() {
		this.startvisible = false;
		this.stateChange(false);
	},
	
	uninitialize : function() {
		dojo.unsubscribe(this.handle);
	}
});;
