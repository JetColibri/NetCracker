var app = angular.module("SpringDemo", []);

app.controller("AppCtrl", function ($scope, $http) {
    $scope.websites = {};
    $http.get('http://localhost:8181/api/user/profile').then(function(response){
        $scope.websites = response.data;
    });
});