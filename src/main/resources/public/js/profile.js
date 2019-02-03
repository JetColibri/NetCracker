var app = angular.module("SpringDemo", []);

var id = window.location.pathname.split( '/profile/' );
console.log(id[1]);
app.controller("AppCtrl", function ($scope, $http) {

    $scope.websites = {};
    $http.get('http://localhost:8181/api/user/'+id[1]).then(function(response){
        $scope.websites = response.data;
    });
});