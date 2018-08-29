var app = angular.module('tdlApp', []);
app.controller('tdlCtrl', function ($scope, $http) {

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

	var serverUrl = "http://localhost:8080";

	$scope.swaggerUrl = serverUrl + "/swagger-ui.html";
	var url = serverUrl + "/catalogue";

	$scope.policies = [];

	$http({
		method: 'GET',
		url: "https://api.github.com/repos/lehmansn/TDLPolicy/contents/policy_types",
		data: { filters: {} },
		headers: { "Content-Type": "application/json" }
	}).then(function (response) {
		for (var i in response.data) {
			$http({
				method: 'GET',
				url: response.data[i].download_url,
				data: { filters: {} },
				headers: { "Content-Type": "text/plain" }
			}).then(function (responsePolicy) {
				// Get data from policytype file of github
				var policyData = responsePolicy.data;
				var policyType = policyData.policy_type.charAt(0).toUpperCase() + policyData.policy_type.substr(1);
				var policyDesc = policyData.description;
				var policyInput = policyData.input;
				var policyExample = policyData.example;

				// Create TabContent 
				var tabContentDiv = document.createElement("div");
				tabContentDiv.id = policyType;
				tabContentDiv.className = "tabcontent";
				// TabContent Header
				var tabContentHeader = document.createElement("h3");
				tabContentHeader.innerHTML = policyType;
				tabContentDiv.appendChild(tabContentHeader);
				// TabContent Description
				var tabContentDescription = document.createElement("p");
				tabContentDescription.innerHTML = policyDesc;
				tabContentDiv.appendChild(tabContentDescription);
				// TabContent Inputvalues Table
				var tabContentTable = document.createElement("table");
				tabContentTable.className = "table table-bordered";
				// TabContent Inputvalues Table Body
				var tabContentTableBody = document.createElement("tbody");
				// TabContent Inputvalues Table Header Row
				var tabContentTableHeaderRow = document.createElement("tr");
				tabContentTableHeaderRow.style.backgroundColor = "#EEE";
				// TabContent Inputvalues Table Header Elements
				var tabContentTableHeaderElementName = document.createElement("th");
				var tabContentTableHeaderElementDatatype = document.createElement("th");
				var tabContentTableHeaderElementDescription = document.createElement("th");
				var tabContentTableHeaderElementValues = document.createElement("th");
				tabContentTableHeaderElementName.innerHTML = "Name";
				tabContentTableHeaderElementDatatype.innerHTML = "Datatype";
				tabContentTableHeaderElementDescription.innerHTML = "Description";
				tabContentTableHeaderElementValues.innerHTML = "Your Values";
				tabContentTableHeaderRow.appendChild(tabContentTableHeaderElementName);
				tabContentTableHeaderRow.appendChild(tabContentTableHeaderElementDatatype);
				tabContentTableHeaderRow.appendChild(tabContentTableHeaderElementDescription);
				tabContentTableHeaderRow.appendChild(tabContentTableHeaderElementValues);
				tabContentTableBody.appendChild(tabContentTableHeaderRow);
				// TabContent Inputvalues Table Policy Input Rows
				for (var index in policyInput) {
					var inputObject = policyInput[index];
					var tabContentTableNewRow = document.createElement("tr");
					var tabContentTableElementName = document.createElement("th");
					var tabContentTableElementDatatype = document.createElement("th");
					var tabContentTableElementDescription = document.createElement("th");
					var tabContentTableElementValues = document.createElement("td");
					tabContentTableElementName.innerHTML = inputObject.value;
					tabContentTableElementDatatype.innerHTML = "[" + inputObject.datatype + "]";
					tabContentTableElementDescription.innerHTML = inputObject.description;

					// Create Input Field
					var inputField = document.createElement("input");
					inputField.className = "input " + policyType;
					inputField.id = inputObject.value;
					switch (inputObject.datatype.toLowerCase()) {
						case "boolean":
							inputField.type = "checkbox";
							break;
						case "enum":
							inputField = document.createElement("select");
							inputField.className = "input " + policyType;
							inputField.id = inputObject.value;
							console.log(inputObject.enum);
							for (var enumIndex in inputObject.enum) {
								var optionField = document.createElement("option");
								optionField.value = inputObject.enum[enumIndex];
								optionField.innerHTML = inputObject.enum[enumIndex];
								inputField.appendChild(optionField);
							}
							break;
						default:
							inputField.type = inputObject.datatype;

					}
					tabContentTableElementValues.appendChild(inputField);
					tabContentTableNewRow.appendChild(tabContentTableElementName);
					tabContentTableNewRow.appendChild(tabContentTableElementDatatype);
					tabContentTableNewRow.appendChild(tabContentTableElementDescription);
					tabContentTableNewRow.appendChild(tabContentTableElementValues);
					tabContentTableBody.appendChild(tabContentTableNewRow);
				}
				tabContentTable.appendChild(tabContentTableBody);
				tabContentDiv.appendChild(tabContentTable);
				tabContentDiv.appendChild(document.createElement("p"));
				// TabContent Add Policy Button
				var tabContentAddButton = document.createElement("button");
				tabContentAddButton.className = "btn btn-success";
				tabContentAddButton.innerHTML = "Add new Policy";
				tabContentAddButton.addEventListener("click", function () {
					// Create Policy Frontend Element
					var topicPoliciesDiv = document.getElementById("topicPolicies");
					var newTopicPolicy = document.createElement("label");
					newTopicPolicy.className = "policy"

					// Create Policy object and add data to Frontend element
					var policy = {
						policyType: policyType,
						policyCategory: policyData.policy_category,
						values: {},
					};
					for (var index = 0; index < document.getElementsByClassName("input " + policyType).length; index++) {
						var element = document.getElementsByClassName("input " + policyType)[index];
						// TODO validate input field
						if (element.type == "checkbox") {
							policy.values[element.id] = element.checked;
						} else {
							policy.values[element.id] = element.value;
						}
					}
					$scope.policies.push(policy);

					newTopicPolicy.id = policy.values.name;
					newTopicPolicy.innerHTML = "<b>" + policy.policyType + "</b>: " + policy.values.name + " ";
					for (var property in policy.values) {
						if (property != "name") {
							newTopicPolicy.innerHTML += "<i>[" + policy.values[property] + "];</i>";
						}
					}
					newTopicPolicy.innerHTML = newTopicPolicy.innerHTML.slice(0, -5);
					newTopicPolicy.innerHTML += "</i> &thinsp;";

					// TODO Remove input field values (Make Empty input fields)

					// Create Remove Button
					var newTopicPolicyRemoveBtn = document.createElement("img");
					newTopicPolicyRemoveBtn.className = "policy-remove-img";
					newTopicPolicyRemoveBtn.src = "images/icon-remove.svg";
					newTopicPolicyRemoveBtn.addEventListener("click", function () {
						if (confirm("Do you really want to delete this Policy?")) {
							// Clear Input
							for (var index = 0; index < document.getElementsByClassName("input " + policyType).length; index++) {
								var element = document.getElementsByClassName("input " + policyType)[index];
								if (element.type == "checkbox") {
									element.checked = false;
								} else {
									element.value = "";
								}
							}
							document.getElementById(policy.values.name).nextElementSibling.remove();
							document.getElementById(policy.values.name).remove();
							for (var index = $scope.policies.length - 1; index >= 0; index--) {
								if ($scope.policies[index].values.name == policy.values.name) {
									$scope.policies.splice(index, 1);
								}
							}
						}
					});
					// Add Element to Frontend
					newTopicPolicy.appendChild(newTopicPolicyRemoveBtn);
					topicPoliciesDiv.appendChild(newTopicPolicy);
					topicPoliciesDiv.appendChild(document.createElement("br"));
				});
				tabContentDiv.appendChild(document.createElement("div").appendChild(tabContentAddButton));
				tabContentDiv.appendChild(document.createElement("p"));
				// TabContent Example Header
				var tabContentExampleHeader = document.createElement("p");
				tabContentExampleHeader.innerHTML = "<i>Example:</i>";
				tabContentDiv.appendChild(tabContentExampleHeader);
				// TabContent Example List
				var tabContentExampleList = document.createElement("ul");
				tabContentExampleList.style = "list-style-type:none";
				// TabContent Example List Elements
				for (var key in policyExample) {
					var tabContentExampleListElement = document.createElement("li");
					tabContentExampleListElement.innerHTML = "<i>" + key + ": " + policyExample[key] + "</i>";
					tabContentExampleList.appendChild(tabContentExampleListElement);
				}
				tabContentDiv.appendChild(tabContentExampleList);

				// Create TabButton
				var tabButton = document.createElement("button");
				tabButton.innerHTML = policyType;
				tabButton.className = "tablinks";
				tabButton.addEventListener("click", function (evt) {
					var policyType = this.innerHTML
					var i, tabcontent, tablinks;
					tabcontent = document.getElementsByClassName("tabcontent");
					for (i = 0; i < tabcontent.length; i++) {
						tabcontent[i].style.display = "none";
					}
					tablinks = document.getElementsByClassName("tablinks");
					for (i = 0; i < tablinks.length; i++) {
						tablinks[i].className = tablinks[i].className.replace(" active", "");
					}
					document.getElementById(policyType).style.display = "block";
					evt.currentTarget.className += " active";
				});

				// Add TabButton
				document.getElementById("PolicyCatalog").appendChild(tabButton);
				// Add TabContent
				document.getElementById("addpolicy").appendChild(tabContentDiv);
			});
		}
	});

	$http({
		method: 'POST',
		url: url + "/search",
		data: { filters: {} },
		headers: { "Content-Type": "application/json" }
	}).then(function (response) {
		$scope.status = response.status;
		$scope.topicDescription = [];

		for (var i in response.data) {
			var elementData = response.data[i];
			$scope.topicDescription.push(angular.fromJson(elementData));
		}

		$scope.backUpTopicDescription = JSON.parse(JSON.stringify($scope.topicDescription));

	}, function (response) {
		$scope.data = response.data || 'Request failed';
		$scope.status = response.status;
	});

	$scope.removeTopicDescription = function (topic) {
		$scope.response = null;

		$http({
			method: 'DELETE',
			url: url + "/delete/" + topic._id.$oid
		}).then(function (response) {
			$scope.status = response.status;
			for (var i in $scope.topicDescription) {
				if ($scope.topicDescription[i]._id.$oid == topic._id.$oid) {
					$scope.topicDescription.splice(i, 1);
					$scope.backUpTopicDescription.splice(i, 1);
					break;
				}
			}
		}, function (response) {
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};

	$scope.cancelUpdateTopicDescription = function (topic) {
		for (var i in $scope.backUpTopicDescription) {
			if ($scope.backUpTopicDescription[i]._id.$oid == topic._id.$oid) {
				$scope.topicDescription[i] = JSON.parse(JSON.stringify($scope.backUpTopicDescription[i]));
				break;
			}
		}
	};

	$scope.updateTopicDescription = function (topic) {
		$scope.response = null;

		var updateBody = {};
		for (var key in topic) {
			if ((key != "_id") && (topic[key] != "")) {
				updateBody[key] = topic[key];
			}
		}

		for (var i in $scope.backUpTopicDescription) {
			if ($scope.backUpTopicDescription[i]._id.$oid == topic._id.$oid) {
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
		).then(function (response) {
			console.log("Update success (200)?");
			$scope.status = response.status;
		}, function (response) {
			console.log("Update failed (400)?");
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};

	$scope.cancelInsertTopicDescription = function () {
		for (var property in $scope.insert) {
			$scope.insert[property] = "";
		}
	};

	$scope.insertTopicDescription = function () {
		$scope.response = null;

		var topicDescription = {};
		for (var property in $scope.insert) {
			var value = $scope.insert[property];
			if (value != "") {
				topicDescription[property] = value;
			}
		}

		// Add Policy to description
		if ($scope.policies.length > 0) {
			topicDescription["policy"] = createPolicyForDescription();
		}

		console.log(topicDescription);
		if ($.isEmptyObject(topicDescription)) {
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
		).then(function (response) {
			$scope.status = response.status;
			$scope.idOfNewTopicDescription = response.data;
			$scope.addDate = new Date();
			topicDescription["_id"] = {};
			topicDescription["_id"]["$oid"] = $scope.idOfNewTopicDescription;
			$scope.topicDescription.push(topicDescription);
			$scope.backUpTopicDescription.push(topicDescription);
		}, function (response) {
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};

	function createPolicyForDescription() {
		var policyForDescription = {};
		for (var index in $scope.policies) {
			var policy = $scope.policies[index];
			var propertyAlreadyExists = false;
			for (var property in policyForDescription) {
				if (property == policy.policyCategory) {
					propertyAlreadyExists = true;
				}
			}
			if (!propertyAlreadyExists) {
				policyForDescription[policy.policyCategory] = [];
			}
			var tempPolicy = {
				policy_type: policy.policyType
			};
			for (var property in policy.values) {
				var value = policy.values[property];
				tempPolicy[property] = value;
			}
			policyForDescription[policy.policyCategory].push(tempPolicy);
		}
		return policyForDescription;
	};

	$scope.getDataTypes = function (topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function (topic, key) {
			var value = topic.data_type;
			if (value != null && !result.includes(value)) {
				result.push(value);
			}
		});

		return result;
	}

	$scope.getProtocol = function (topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function (topic, key) {
			var value = topic.protocol;
			if (value != null && !result.includes(value)) {
				result.push(value);
			}
		});

		return result;
	}

	$scope.getMessageFormat = function (topicDescriptions) {
		var result = [];

		angular.forEach(topicDescriptions, function (topic, key) {
			var value = topic.message_format;
			if (value != null && !result.includes(value)) {
				result.push(value);
			}
		});

		return result;
	}
});