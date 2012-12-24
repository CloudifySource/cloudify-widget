cloudify-widget
===============

The Cloudify Widget is a server that allows to a Cloudify "recipe player" in other website, and allow visitors to launch a certain recipe with a single click. It uses [HPCloud](http://www.hpcloud.com) to provision VMs and host the installed recipes everytime a user launches a recipe using the widget. Provisioning of the VMs and the interaction with the cloud APIs is done via the [jclouds](http://www.jclouds.org) library.

For more information on how to create a widget and embed it in your web pages, and how to setup the widget server, please refer to the project's [wiki](https://github.com/CloudifySource/cloudify-widget/wiki).

