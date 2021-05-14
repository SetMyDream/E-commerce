'use strict';

var app = angular.module('ChatApp', []);

app.controller('MainCtrl', function ($scope, $http, settings) {
    $scope.usernameInput = 'User' + Math.floor(Math.random() * 99 + 1);
    $scope.username = null;
    $scope.messageInput = '';
    $scope.messages = [];

    $scope.setUsername = function () {
        $scope.usernameInput = $scope.usernameInput.trim();
        if ($scope.usernameInput === '') {
            alert('Username cannot be empty!');
            return;
        }

        $scope.username = $scope.usernameInput;
    };

    $scope.sendMessage = function () {
        if ($scope.messageInput.trim() === '') {
            alert('Message cannot be empty!');
            return;
        }

        $http.post(settings.messageUrl, {
            username: $scope.username,
            content: $scope.messageInput
        });

        $scope.messageInput = '';
    };

    $scope.sendMessageOnEnter = function ($event) {
        if ($event.keyCode == 13 && !$event.shiftKey) {
            $scope.sendMessage();
            $event.preventDefault();
            return false;
        }
    };

    $scope.backToLogin = function () {
        $scope.username = null;
    };

    var feed = new EventSource(settings.feedUrl);
    feed.addEventListener('message', function (response) {
        $scope.$apply(function () {
            $scope.messages.push(JSON.parse(response.data));
            $scope.$emit('messageReceived');
        });
    });
});

app.directive('ngFocusIf', function ($timeout) {
    return function (scope, element, attrs) {
        scope.$watch(
            attrs.ngFocusIf,
            function (newValue) {
                $timeout(function () {
                    newValue && element.focus();
                });
            },
            true
        );
    };
});
app.directive('ngScrollToBottom', function ($document, $timeout) {
    return function (scope, element, attrs) {
        scope.$on(attrs.ngScrollToBottom, function (e) {
            $timeout(function () {
                element.animate({
                    scrollTop: $document.height()
                }, 'slow');
            });
        });
    };
});