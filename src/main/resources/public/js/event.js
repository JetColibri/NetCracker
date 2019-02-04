var app = angular.module("SpringDemo", []);

var id = window.location.pathname.split( '/event/' );
app.controller("AppCtrl", function ($scope, $http) {

    $scope.websites = {};
    $http.get('http://localhost:8181/api/event/'+id[1]).then(function(response){
        $scope.websites = response.data;
    });
});
