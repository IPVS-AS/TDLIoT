var app = angular.module('tdlApp', []);
app.controller('tdlCtrl', function($scope, $http) {
	
	$scope.topicDescriptionProperties = [
		"data_type",
		"hardware_type",
		"topic_type",
		"message_format",
		"message_structure",
		"protocol",
		"owner",
		"middleware_endpoint",
		"path"
	]

	var serverUrl = "http://192.168.209.199:8080";
	
	$scope.swaggerUrl = serverUrl + "/swagger-ui.html";	
	var url = serverUrl + "/catalogue";
	
	$http({
    	method: 'POST', 
    	url: url + "/search",
    	data: { filters: {}},
    	headers: { "Content-Type": "application/json" }
    }).then(function(response) {
    	$scope.status = response.status;    	
    	$scope.topicDescription = [];
    	
    	for(var i in response.data){
    		var elementData = response.data[i];
    		$scope.topicDescription.push(angular.fromJson(elementData)); 
    	}   
    	
    	$scope.backUpTopicDescription = JSON.parse(JSON.stringify($scope.topicDescription));
    	
    }, function(response) {
        $scope.data = response.data || 'Request failed';
        $scope.status = response.status;
    });
	
	$scope.removeTopicDescription = function(topic) {
        $scope.response = null;

        $http({
        	method: 'DELETE', 
        	url: url + "/delete/" + topic._id.$oid
        }).then(function(response) {
	    	$scope.status = response.status;
   			for(var i in $scope.topicDescription){
   				if($scope.topicDescription[i]._id.$oid == topic._id.$oid){
   					$scope.topicDescription.splice(i, 1);
   					$scope.backUpTopicDescription.splice(i, 1);
   					break;
   				}
	    	} 
	    }, function(response) {
	        $scope.data = response.data || 'Request failed';
	        $scope.status = response.status;
	    });
	};
	
	$scope.cancelUpdateTopicDescription = function(topic) {      
		for(var i in $scope.backUpTopicDescription){
			if($scope.backUpTopicDescription[i]._id.$oid == topic._id.$oid){
				$scope.topicDescription[i] = JSON.parse(JSON.stringify($scope.backUpTopicDescription[i]));
				break;
			}
        } 
	};
	
	$scope.updateTopicDescription = function(topic) {
		$scope.response = null;
        
		var updateBody = {};
		for(var key in topic){
			if(key != "_id"){
				updateBody[key] = topic[key];
			}
		}
        
		for(var i in $scope.backUpTopicDescription){
			if($scope.backUpTopicDescription[i]._id.$oid == topic._id.$oid){
				$scope.backUpTopicDescription[i] = JSON.parse(JSON.stringify(topic));
				break;
			}
		} 
        
		$http({
			method: 'PUT', 
			url: url + "/update/" + topic._id.$oid,
			data: updateBody,
			headers: { "Content-Type": "application/json" }
		}
		).then(function(response) {
			$scope.status = response.status;
		}, function(response) {
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};
	
	$scope.cancelInsertTopicDescription = function() {      
		for(var property in $scope.insert){
			$scope.insert[property] = "";
		}   
	};
	
	$scope.insertTopicDescription = function() {
        $scope.response = null;
        
        var topicDescription = {};
        for(var property in $scope.insert){
        	var value = $scope.insert[property];
        	if(value != ""){
        		topicDescription[property] = value;
        	}
    	}     
        
        if($.isEmptyObject(topicDescription)){
        	alert('No values available! Please insert values.');
        	return;
        }
	
    	$http({
        	method: 'POST', 
        	url: url + "/add",
        	data: topicDescription,
        	headers: { "Content-Type": "text/plain" },
        	transformResponse: [function (data) {
       	      return data;
       	  }]
    	}
        ).then(function(response) {
	    	$scope.status = response.status;
	    	$scope.idOfNewTopicDescription = response.data;
	    	$scope.addDate = new Date();
	    	topicDescription["_id"] = {};
	        topicDescription["_id"]["$oid"] = $scope.idOfNewTopicDescription;
			$scope.topicDescription.push(topicDescription);
			$scope.backUpTopicDescription.push(topicDescription);
	    }, function(response) {
	        $scope.data = response.data || 'Request failed';
	        $scope.status = response.status;
	    });
	};
	
	$scope.getDataTypes = function(topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function(topic, key) {
			var value = topic.data_type;
			if(value != null && !result.includes(value)){
				result.push(value);	
			}
		});

		return result;
	}
	
	$scope.getProtocol = function(topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function(topic, key) {
			var value = topic.protocol;
			if(value != null && !result.includes(value)){
				result.push(value);	
			}
		});

		return result;
	}
	
	$scope.getMessageFormat = function(topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function(topic, key) {
			var value = topic.message_format;
			if(value != null && !result.includes(value)){
				result.push(value);	
			}
		});

		return result;
	}
});