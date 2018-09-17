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
		"path",
		"policy"
	]

	$scope.searchText = {
		"_id": {
			"$oid": ""
		},
		"hardware_type": "",
		"topic_type": "",
		"message_format": "",
		"message_structure": "",
		"protocol": "",
		"owner": "",
		"middleware_endpoint": "",
		"path": "",
		"policy": ""

	}
	var serverUrl = "http://localhost:8080";

	$scope.swaggerUrl = serverUrl + "/swagger-ui.html";
	var url = serverUrl + "/catalogue";

	$scope.policies = [];

	$scope.operator = "equal";
	$scope.filters = [];

	getTopicDescriptionsByFilter();

	// Get policies from github repo
	$http({
		method: 'GET',
		url: "https://api.github.com/repos/lehmansn/TDLPolicy/contents/policy_types",
		headers: { "Content-Type": "application/json" }
	}).then(function (response) {
		for (var i in response.data) {
			$http({
				method: 'GET',
				url: response.data[i].download_url,
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
						// TODO validate input field?
						switch (element.type) {
							case "checkbox":
								policy.values[element.id] = element.checked;
								break;
							case "int":
								policy.values[element.id] = parseInt(element.value);
								break;
							case "number":
								policy.values[element.id] = parseFloat(element.value);
								break;
							default:
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

					for (var index = 0; index < document.getElementsByClassName("input " + policyType).length; index++) {
						var element = document.getElementsByClassName("input " + policyType)[index];
						element.value = "";
					}

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

	$scope.searchInsidePolicy = function (id, searchText) {
		for (var i in $scope.topicDescription) {
			if ($scope.topicDescription[i]._id.$oid == id) {
				topicDescription[i].policy.includes(searchText);
				break;
			}
		}
	}
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
			successNotifiction("Successfully removed topic description");
		}, function (response) {
			switch (response.status) {
				case -1:
					dangerNotifiction("Server is not available!");
					break;
				default:
					dangerNotifiction(response.data);
					break;
			}
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
			successNotifiction("Successfully updated topic description");
		}, function (response) {
			console.log("Update failed (400)?");
			switch (response.status) {
				case -1:
					dangerNotifiction("Server is not available!");
					break;
				default:
					dangerNotifiction(response.data);
					break;
			}
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};

	$scope.cancelInsertTopicDescription = function () {
		for (var property in $scope.insert) {
			$scope.insert[property] = "";
		}
		$scope.policies = [];
		var policyDiv = document.getElementById("topicPolicies");
		while (policyDiv.firstChild) {
			policyDiv.removeChild(policyDiv.firstChild);
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

		if ($.isEmptyObject(topicDescription)) {
			dangerNotifiction("No values available! Please insert values.");
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
			successNotifiction("Successfully added topic description");
		}, function (response) {
			switch (response.status) {
				case -1:
					dangerNotifiction("Server is not available!");
					break;
				default:
					dangerNotifiction(response.data);
					break;
			}
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	};

	function createPolicyForDescription() {
		var policyForDescription = {};
		for (var index in $scope.policies) {
			var policy = $scope.policies[index];
			var categoryAlreadyExists = false;
			for (var category in policyForDescription) {
				if (category == policy.policyCategory) {
					categoryAlreadyExists = true;
				}
			}
			if (!categoryAlreadyExists) {
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

	$scope.addFilter = function () {
		var attributeName = document.getElementById("filterAttributeName").value.trim();
		var firstFilterValue;
		var secondfilterValue;
		var filterUIinnerHTML;
		var operator;
		switch ($scope.operator) {
			case "equal":
				operator = "$eq";
				if (isNaN(parseFloat(document.getElementById("firstFilterValue").value.trim()))) {
					firstFilterValue = document.getElementById("firstFilterValue").value.trim();
				} else {
					firstFilterValue = parseFloat(document.getElementById("firstFilterValue").value.trim());

				}
				filterUIinnerHTML = attributeName + " = " + firstFilterValue;
				break;
			case "greater":
				operator = "$gt";
				firstFilterValue = parseFloat(document.getElementById("firstFilterValue").value.trim());
				filterUIinnerHTML = attributeName + " &gt; " + firstFilterValue;
				break;
			case "smaller":
				operator = "$lt";
				firstFilterValue = parseFloat(document.getElementById("firstFilterValue").value.trim());
				filterUIinnerHTML = attributeName + " &lt; " + firstFilterValue;
				break;
			case "range":
				// custom operator because mongoDB has not any single operator for range
				operator = "$ra";
				firstFilterValue = parseFloat(document.getElementById("firstFilterValue").value.trim());
				secondfilterValue = parseFloat(document.getElementById("secondFilterValue").value.trim());
				filterUIinnerHTML = firstFilterValue + " &gt; " + attributeName + " &lt; " + secondfilterValue;
				break;
			case "unequal":
				operator = "$ne";
				if (isNaN(parseFloat(document.getElementById("firstFilterValue").value.trim()))) {
					firstFilterValue = document.getElementById("firstFilterValue").value.trim();
				} else {
					firstFilterValue = parseFloat(document.getElementById("firstFilterValue").value.trim());

				}
				filterUIinnerHTML = attributeName + " != " + firstFilterValue;
				break;
		}
		var singleFilterObject = {
			attribute: attributeName,
			operator: operator,
			firstFilter: firstFilterValue,
			secondFilter: secondfilterValue
		};
		$scope.filters.push(singleFilterObject);
		var filterExpressionIndex = $scope.filters.length - 1;
		// Create Filter Frontend Element
		var filterDiv = document.getElementById("filtersDiv");
		var newFilterElement = document.createElement("label");
		newFilterElement.className = "single-filter"
		newFilterElement.innerHTML = filterUIinnerHTML;

		// Create Remove Button
		var removeBtn = document.createElement("img");
		removeBtn.className = "filter-remove-img";
		removeBtn.src = "images/icon-remove.svg";
		removeBtn.addEventListener("click", function () {
			if (confirm("Do you really want to delete this Filter?")) {
				filterDiv.removeChild(newFilterElement)
				$scope.filters.splice(filterExpressionIndex, 1);
				getTopicDescriptionsByFilter();
			}
		});
		newFilterElement.appendChild(removeBtn);
		filterDiv.appendChild(newFilterElement);

		// Clear input elements
		$scope.operator = "equal";
		document.getElementById("filterAttributeName").value = "";
		document.getElementById("firstFilterValue").type = "string";
		document.getElementById("firstFilterValue").value = "";
		document.getElementById("secondFilterValue").value = "";
		document.getElementById("secondFilterValue").style.display = "none"
		getTopicDescriptionsByFilter();
	}

	$scope.clearFilter = function () {
		var filterDiv = document.getElementById("filtersDiv");
		while (filterDiv.firstChild) {
			filterDiv.removeChild(filterDiv.firstChild);
		}
		$scope.filters = [];
		getTopicDescriptionsByFilter();
	}

	function getTopicDescriptionsByFilter() {
		var filter = {};
		for (var i = 0; i < $scope.filters.length; i++) {
			var currentFilter = $scope.filters[i];
			if (!filter.hasOwnProperty(currentFilter.attribute)) {
				filter[currentFilter.attribute] = {};
			}
			if (currentFilter.operator == "$ra") {
				filter[currentFilter.attribute]["$gt"] = currentFilter.firstFilter;
				filter[currentFilter.attribute]["$lt"] = currentFilter.secondFilter;
			} else {
				filter[currentFilter.attribute][currentFilter.operator] = currentFilter.firstFilter;
			}
		}

		$http({
			method: 'POST',
			url: url + "/search",
			data: filter,
			headers: { "Content-Type": "application/json" }
		}).then(function (response) {
			$scope.status = response.status;
			$scope.topicDescription = [];

			for (var i in response.data) {
				var elementData = response.data[i];
				$scope.topicDescription.push(angular.fromJson(elementData));
			}

			$scope.backUpTopicDescription = JSON.parse(JSON.stringify($scope.topicDescription));

			successNotifiction("Successfully recvied filtered topic description");
		}, function (response) {
			switch (response.status) {
				case -1:
					dangerNotifiction("Server is not available!");
					break;
				default:
					dangerNotifiction(response.data);
					break;
			}
			$scope.data = response.data || 'Request failed';
			$scope.status = response.status;
		});
	}

	$scope.checkFilterOperator = function () {
		switch ($scope.operator) {
			case "equal":
				document.getElementById("firstFilterValue").type = "string";
				document.getElementById("firstFilterValue").placeholder = "value";
				document.getElementById("secondFilterValue").style.display = "none";
				break;
			case "greater":
				document.getElementById("firstFilterValue").type = "number";
				document.getElementById("firstFilterValue").placeholder = "value";
				document.getElementById("secondFilterValue").style.display = "none";
				break;
			case "smaller":
				document.getElementById("firstFilterValue").type = "number";
				document.getElementById("firstFilterValue").placeholder = "value";
				document.getElementById("secondFilterValue").style.display = "none";
				break;
			case "range":
				document.getElementById("firstFilterValue").type = "number";
				document.getElementById("firstFilterValue").placeholder = "first value";
				document.getElementById("secondFilterValue").style.display = "block";
				break;
			case "unequal":
				document.getElementById("firstFilterValue").type = "string";
				document.getElementById("firstFilterValue").placeholder = "value";
				document.getElementById("secondFilterValue").style.display = "none";
				break;
		}
	}

	$scope.setSearchTextPolicy = function () {
		console.log("setPolicy");
	}

	$scope.filterTopics = function (topic) {
		var showTopic = true;
		for (var key in $scope.searchText) {
			if ($scope.searchText[key] != undefined) {
				switch (key) {
					case "_id":
						if ($scope.searchText[key]["$oid"] != "") {
							if (topic[key]["$oid"] != undefined) {
								if (!topic[key]["$oid"].includes($scope.searchText[key]["$oid"])) {
									showTopic = false;
								}
							} else {
								showTopic = false;
							}
						}
						break;
					case "policy":
						if ($scope.searchText[key] != "") {
							if (topic[key] != undefined) {
								if (!JSON.stringify(topic[key]).includes($scope.searchText[key])) {
									showTopic = false;
								}
							} else {
								showTopic = false;
							}
						}
						break;
					default:
						if ($scope.searchText[key] != "") {
							if (topic[key] != undefined) {
								if (!topic[key].includes($scope.searchText[key])) {
									showTopic = false;
								}
							} else {
								showTopic = false;
							}
						}

				}
			}
		}
		return showTopic;
	}

	function dangerNotifiction(message) {
		$.notify({
			message: message
		}, {
				type: "danger",
				newest_on_top: true,
				delay: 0
			});
	}

	function successNotifiction(message) {
		$.notify({
			message: message
		}, {
				type: "success",
				newest_on_top: true,
			});
	}
});