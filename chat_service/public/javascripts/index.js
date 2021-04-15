$(document).ready(function() {
 if ("WebSocket" in window) {
      console.log("WebSocket is supported by your Browser!");
 } else {
 	console.log("WebSocket NOT supported by your Browser!");
   	return;
 }

 let url = $('body').attr('data-url');
 let ws;

 const onWsMessage = (event) => {
    let message = JSON.parse(event.data);
    switch (message.type) {
      case "topichistory":
        populateTopicHistory(message);
        break;
      case "topicupdate":
        updateTopic(message);
        break;
      case "topiclist":
        updateTopicList(message);
        break;        
      default:
        console.log(message);
      	break;
    }
 };

 const onWsOpen = (event) => {
	 console.log("WebSocket Open");	 
 };

 const onWsError = (event) => {	 
	 console.log("WebSocket Error");
	 $("#chatrooms").empty();
 };

 const onWsClose = (event) => {
	 console.log("WebSocket Close");
	 $("#chatrooms").empty();
 };

 const getWebsocket = () => {
	 if (!ws || ws.readyState === ws.CLOSED) {		 
		 console.log("restarting websocket");
		 ws = new WebSocket(url);
		 ws.onmessage = onWsMessage;
		 ws.onerror = onWsError;
		 ws.onopen = onWsOpen;
		 ws.onclose = onWsClose;
	 }
	 return ws;
 };

 
 const submitAction = (event) => {
	    event.preventDefault();
	    
	    // if new topic is inserted pick it otherwise default to select
	    let topicName = $("#addtopictext").val() ? $("#addtopictext").val() : $("#topicSelect").val();
	    
	    // send the message to add new text to the topic
	    getWebsocket().send(JSON.stringify({
	    		topic: topicName,
	    		msg: $("#chattext").val(),
	    		msgType: "chat"
	    }));
	    	    
	    // reset the form fields
	    $("#chattext").val("");
	    $("#addtopictext").val("");	    
 };

 const populateTopicHistory = (message) => {
	  $("#username").text(message.user);
	  
	  // creates chat box container on the fly
	  let chatDiv = $("<div>").addClass("container").addClass("chatbox")
	  		.prop("id", "chatbox_" + message.topic);
	  let chatRow = $("<div>").addClass("row");
	  
	  let panelInfo = $("<div>").addClass("panel").addClass("panel-info");
	  
	  let panelInfoShow = $("<span>").addClass("pull-right").text("subscribe ");
	  let subscribeCheckbox = $("<input type='checkbox' checked>");
	  
	  // avoid using (event) => arrow style due to this reference resolution 
	  subscribeCheckbox.click(function(event) {
		  if(this.checked) {
			  handleWatchTopic(message.topic);  
	      } else {
	    	  handleUnwatchTopic(message.topic);
	      }	                		  	    
	  });

	  let exitChat = $("<span>").addClass("pull-right")
		.append(subscribeCheckbox);

	  let panelInfoHeader = $("<div>").addClass("panel-heading")
	  			.text(message.topic)
	  			.append(panelInfoShow)
	  			.append(exitChat);
	  panelInfo.append(panelInfoHeader);
	  
	  let panelBody = $("<div>").addClass("panel-body");
	  panelBody.append($("<ul>").addClass("media-list").prop("id", "topic_" + message.topic));
	  panelInfo.append(panelBody);
	  
	  let chatSection = $("<div>").addClass("col-md-8")
	  			.append(panelInfo);
	  chatRow.append(chatSection);

	  // top 10 users score panel
	  let userSection = $("<div>").addClass("col-md-4");
	  let userInfo = $("<div>").addClass("panel").addClass("panel-primary");
	  let userInfoHeader = $("<div>").addClass("panel-heading").text("Top 10 User scores");
	  let userInfoShow = $("<span>").addClass("pull-right").text("view ");	  
	  let userInfoCheckbox = $("<input type='checkbox' checked>");		 
	  userInfoCheckbox.change(function() {		  
		  if(this.checked) {
	        	$('#topusers_' + message.topic).show();
	      } else {
	        	$('#topusers_' + message.topic).hide();
	      }	                
	  });
	    
	  userInfoShow.append(userInfoCheckbox);
	  userInfoHeader.append(userInfoShow);	  
	  userInfo.append(userInfoHeader);
	  
	  let userInfoBody = $("<div>").addClass("panel-body");
	  let userList = $("<ul>").addClass("media-list").prop("id", "topusers_" + message.topic);	  
	  userInfoBody.append(userList);	  
	  userInfo.append(userInfoBody);

	  userSection.append(userInfo);
	  chatRow.append(userSection);

	  chatDiv.append(chatRow);
	  $("#chatrooms").prepend(chatDiv);

	  let topicSelect = $("#topicSelect");
	  topicSelect.append('<option value=' + message.topic + '>' + message.topic + '</option>');
	  
	  // select newly created topic
	  topicSelect.val(message.topic);
};

const createChatLine = (message) => {
	  let chatLine = $("<li>").addClass("media");
	  let mediaBody = $("<div>").addClass("media-body");
	  let media = $("<div>").addClass("media");
	  let innerMediaBody = $("<div>").addClass("media-body");
	  innerMediaBody.text(message);
	  media.append(innerMediaBody);
	  mediaBody.append(media);
	  chatLine.append(mediaBody);
	  return chatLine;
};

const createScoreList = (topic, scores) => {
	
	let elem = $("#topusers_" + topic);
	elem.empty();
	for (let s of scores) {
		let scoreLine = $("<li>").addClass("media");
		let scoreMediaBody = $("<div>").addClass("media-body");
		let scoreMedia = $("<div>").addClass("media");
		let scoreMediaBodyInner = $("<div>").addClass("media-body");
		let userScore = $("<h5>").text(s[0] + " | " + s[1]);		
		scoreMediaBodyInner.append(userScore);
		scoreMedia.append(scoreMediaBodyInner);
		scoreMediaBody.append(scoreMedia);
		scoreLine.append(scoreMediaBody);
		elem.append(scoreLine);
	}
	
} 

const updateTopic = (message) => {
  let chatLine = createChatLine(message.msg);
  $('#topic_'+message.topic).append(chatLine);    
	
  createScoreList(message.topic, message.scores); 

  // animation to scroll down to latest msg
  $('#chatbox_'+message.topic).stop().animate({
	    scrollTop: chatLine.offset().top
	  }, '500', 'swing', function() {}
  );
    
};

const updateTopicList = (message) => {
	let elem = $("#topicSelect");
	let currVal = elem.val();
	
	elem.empty();
	for (let t of message.topics) {
		elem.append('<option value=' + t + '>' + t + '</option>');		
	}
	elem.val(currVal);
};

const handleUnwatchTopic = (topicName) => {
    // send the message to add new text to the topic
    getWebsocket().send(JSON.stringify({
    		topic: topicName,
    		msg: "",
    		msgType: "unwatch"
    }));
    $('#chatbox_' + topicName).remove();
    $("#topicSelect option[value='"+ topicName +"']").remove();
};

const handleWatchTopic = (topicName) => {
    // send the message to add new text to the topic
    getWebsocket().send(JSON.stringify({
    		topic: topicName,
    		msg: "",
    		msgType: "chat"
    }));
};

 // form listener
 $("#chatform").submit(submitAction);

 // open connection to start
 ws = getWebsocket();
});