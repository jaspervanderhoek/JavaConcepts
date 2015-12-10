dojo.provide("HelpText.widget.HelpTextRowAttr");
dojo.require("HelpText.widget.HelpTextRow")

mxui/widget.declare('HelpText.widget.HelpTextRowAttr', {
	superclass  : HelpText.widget.HelpTextRow,
    addons      : [],
	
	inputargs: {
		name : '',
		startvisible : false,
		text : '',
		startvisible : false,
		height : 300,
		hideonclick : false
	},
	
    text : '',
    
    _setValueAttr : function(value) {
        this.text = mxui/dom.escapeString(value).replace(/\n/g,"<br/>");;
		dojo.html.set(this.domNode, this.text);
	}
    
});
