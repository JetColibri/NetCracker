var app = angular.module("SpringDemo", []);
var id = new URLSearchParams(window.location.search).getAll('id');
console.log(id);
app.controller("AppCtrl", function ($scope, $http) {

    $scope.websites = {};
    $http.get('http://localhost:8181/api/user/profile?id='+id).then(function(response){
        $scope.websites = response.data;
    });
});