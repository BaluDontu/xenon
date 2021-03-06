# XENON UI

A developer-centric application that makes it easier to navigate around the xenon nodes and clusters, and monitor the documents.

# Table of Contents

- [Introduction](#introduction)
    - [Availability](#availability)
    - [Available Features](#available-features)
    - [Planned Improvements](#planned-improvements)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Usage](#usage)
    - [NativeScript App](#nativescript-app)
    - [Electron App](#electron-app)
    - [Testing](#testing)
    - [Web Configuration Options](#web-configuration-options)
    - [Directory Structure](#directory-structure)
    - [Dependencies & Integrations](#dependencies-integrations)
- [Cut a Release](#cut-a-release)
    - [Release as a web application](#release-as-a-web-application)
    - [Release as a desktop application](#release-as-a-desktop-application)
- [Contributing](#contributing)


# Introduction

Xenon UI is built for one purpose: make your Xenon development experience MUCH better. It provides a rich set of features that can help you:
- Debug your Xenon-based system by organizing various documents and visualizations them in a meaningful way.
- Navigate to any node within the node group and browse node-specific document contents.
- Make REST calls with test payloads.
- Generate query tasks interactively.
- Trace operations.
- Extract logs that are specific to each node.

The application is still in its early stage of development and is expected to iterate/change rapidly.

#### Availability

Xenon UI comes as a web application with every Xenon jar after 0.9.6 and is available in /core/ui/default. In future we will also consider distributing it as a standalone desktop application.

#### Available Features

- Node Selector: displays all the peer nodes and their system info in the current group, and you can select a specific node so the UI shows all the information relevant to this node.
- Dashboard: shows stats for CPU, Memory and Disk usage, and logs.
- Lists all the available factory services and their status and options, as well as detailed information of service instances under the factory services.
- Create, Edit(PATCH/PUT) and Delete instances.
- Logs for each node.
- Query UI that allows building queries using either the interactive "Query Builder" or JSON editor, and displays the results right below.
- Login/logout mechanism.
- Conceptual about page, we can either create a page like this with documentations or point the ? icon to xenon wiki (which would be easier).
- (PARTIAL) i18n support

#### Planned Improvements

- Operation Tracing.
- Pagination: not implemented for any of the lists, which will cause performance issue when it scales. Need some refactoring once having a clearer idea on how Xenon does it.
- Reactive: right now data all come from one off http calls, no change will be reflected on the UI without page refresh. Need some work here.
- Node Selector: Need to cover more complex topology scenarios.
- Dashboard improvements: Aggregated service instance stats and index usage stats needs to be added to the dashboard.
- Form improvements: UX revisit and validation on Create, Edit and Delete instance model forms.
- System configuration: should allow edits of some system properties.
- Performance: definitely need some tuning.
- Improvements/fixes based on your feedback.
- Browser compatibility: as of now the UI has only been developed and tested against Chrome, need to make it work cross browser.

# Getting Started

## Prerequisites

* node v5.x.x or higher and npm 3 or higher.

* To run the NativeScript app:

```bash
npm install -g nativescript
npm install -g typescript
```
## Usage

```bash
# install the project's dependencies
npm install
# watches your files and uses livereload by default
npm start
# api document for the app
npm run serve.docs

# to start deving with livereload site and coverage as well as continuous testing
npm run start.deving

# dev build
npm run build.dev
# prod build
npm run build.prod
# prod build with AoT compilation
npm run build.prod.exp
```

## Special Note About AoT

When using `npm run build.prod.exp` for AoT builds, please consider the following:

Currently you cannot use custom component decorators with AoT compilation. This may change in the future but for now you can use this pattern for when you need to create AoT builds for the web:

```
import { Component } from '@angular/core';
import { BaseComponent } from '../frameworks/core/index';

// @BaseComponent({   // just comment this out and use Component from 'angular/core'
@Component({
  // etc.
```

After doing the above, running AoT build via `npm run build.prod.exp` will succeed. :)

`BaseComponent` custom component decorator does the auto `templateUrl` switching to use {N} views when running in the {N} app therefore you don't need it when creating AoT builds for the web. However just note that when going back to run your {N} app, you should comment back in the `BaseComponent`. Again this temporary inconvenience may be unnecessary in the future.

## NativeScript App

#### Setup

```bash
npm install -g nativescript
```

#### Dev Workflow

You can make changes to files in `src/client` or `nativescript` folders. A symbolic link exists between the web `src/client` and the `nativescript` folder so changes in either location are mirrored because they are the same directory inside.

Create `.tns.html` and `.tns.css` NativeScript view files for every web component view file you have.

#### Run

```
iOS:                      npm run start.ios
iOS (livesync emulator):  npm run start.livesync.ios
iOS (livesync device):    npm run start.livesync.ios.device

// or...

Android:                      npm run start.android
Android (livesync emulator):  npm run start.livesync.android
Android (livesync device):    npm run start.livesync.android.device
```

* Requires an image setup via AVD Manager. [Learn more here](http://developer.android.com/intl/zh-tw/tools/devices/managing-avds.html) and [here](https://github.com/NativeScript/nativescript-cli#the-commands).

OR...

* [GenyMotion Android Emulator](https://www.genymotion.com/)

##### Building with Webpack for release builds

You can greatly reduce the final size of your NativeScript app by the following:

```
cd nativescript
npm i nativescript-dev-webpack --save-dev
```
Then you will need to modify your components to *not* use `moduleId: module.id` and change `templateUrl` to true relative app, for example:

before:

```
@BaseComponent({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css']
})
```
after:

```
@BaseComponent({
  // moduleId: module.id,
  selector: 'sd-home',
  templateUrl: './app/components/home/home.component.html',
  styleUrls: ['./app/components/home/home.component.css']
})
```

Then to build:

Ensure you are in the `nativescript` directory when running these commands.

* iOS: `WEBPACK_OPTS="--display-error-details" tns build ios --bundle`
* Android: `WEBPACK_OPTS="--display-error-details" tns build android --bundle`

Notice your final build will be drastically smaller. In some cases 120 MB -> ~28 MB.

## Electron App

#### Develop

```
Mac:      npm run start.desktop
Windows:  npm run start.desktop.windows
```

#### Develop with livesync
```
Mac:      npm run start.livesync.desktop
Windows:  npm run start.livesync.desktop.windows
```

#### Release: Package Electron App for Mac, Windows or Linux

```
Mac:      npm run build.desktop.mac
Windows:  npm run build.desktop.windows
Linux:    npm run build.desktop.linux
```

## Testing

```bash
$ npm test

# Development. Your app will be watched by karma
# on each change all your specs will be executed.
$ npm run test.watch
# NB: The command above might fail with a "EMFILE: too many open files" error.
# Some OS have a small limit of opened file descriptors (256) by default
# and will result in the EMFILE error.
# You can raise the maximum of file descriptors by running the command below:
$ ulimit -n 10480


# code coverage (istanbul)
# auto-generated at the end of `npm test`
# view coverage report:
$ npm run serve.coverage

# e2e (aka. end-to-end, integration) - In three different shell windows
# Make sure you don't have a global instance of Protractor

# npm install webdriver-manager <- Install this first for e2e testing
# npm run webdriver-update <- You will need to run this the first time
$ npm run webdriver-start
$ npm run serve.e2e
$ npm run e2e

# e2e live mode - Protractor interactive mode
# Instead of last command above, you can use:
$ npm run e2e.live
```
You can learn more about [Protractor Interactive Mode here](https://github.com/angular/protractor/blob/master/docs/debugging.md#testing-out-protractor-interactively)

## Web Configuration Options

Default application server configuration

```javascript
var PORT             = 5000;
var LIVE_RELOAD_PORT = 4002;
var DOCS_PORT        = 4003;
var APP_BASE         = '/';
```

Configure at runtime

```bash
npm start -- --port 8080 --reload-port 4000 --base /my-app/
```

## Directory Structure

```
.
├── src                        <- source code of the application
│   └── client
│       ├── app
│       │   ├── components     <- application specific components
│       │   └── frameworks     <- shared components and services
│       │   │   ├── analytics  <- analytics provided by Segment(https://segment.com/)
│       │   │   ├── app        <- shared application architecture code
│       │   │   ├── core       <- foundation layer (decorators and low-level services)
│       │   │   ├── electron   <- electron(http://electron.atom.io/) specific code
│       │   │   ├── i18n       <- internationalization features
│       │   │   └── test       <- test specific code providing conveniences
│       ├── assets             <- application assets, fonts, images, etc.
│       ├── css                <- application level css
│       ├── testing
│       ├── index.html
│       ├── main.desktop.ts    <- main ts for building desktop application
│       ├── main.web.prod.ts   <- main ts for building web application in production
│       ├── main.web.ts        <- main ts for building web application
│       ├── package.json       <- package.json for building desktop application
│       ├── system.config.ts
│       ├── tsconfig.json
│       ├── typings.d.ts
│       └── web.modules.ts
├── tools
│   ├── README.md              <- build documentation
│   ├── config
│   │   ├── project.config.ts  <- configuration specific to xenon
│   │   ├── seed-advanced.config.ts
│   │   ├── seed.config.interfaces.ts
│   │   └── seed.config.ts     <- generic configuration of the project
│   ├── config.ts              <- exported configuration
│   ├── debug.ts
│   ├── env                    <- environment configuration
│   ├── manual_typings
│   │   ├── project            <- manual ambient typings specific to xenon
│   │   │   └── sample.package.d.ts
│   │   └── seed               <- generic manual ambient typings
│   ├── tasks                  <- gulp tasks
│   │   ├── project            <- xenon specific gulp tasks
│   │   │   └── sample.task.ts
│   │   └── seed               <- generic gulp tasks. They can be overriden by the xenon specific gulp tasks
│   ├── utils                  <- build utils
│   │   ├── project            <- xenon specific gulp utils
│   │   │   └── sample_util.ts
│   │   ├── project.utils.ts
│   │   ├── seed               <- generic gulp utils
│   │   │   ├── clean.ts
│   │   │   ├── code_change_tools.ts
│   │   │   ├── server.ts
│   │   │   ├── tasks_tools.ts
│   │   │   ├── template_locals.ts
│   │   │   ├── tsproject.ts
│   │   │   └── watch.ts
│   │   └── seed.utils.ts
│   └── utils.ts
├── README.md
├── gulpfile.ts                <- configuration of the gulp tasks
├── karma.conf.js              <- configuration of the test runner
├── package.json               <- dependencies of the project
├── protractor.conf.js         <- e2e tests configuration
├── test-main.js               <- testing configuration
├── tsconfig.json              <- configuration of the typescript project (ts-node, which runs the tasks defined in gulpfile.ts)
├── tslint.json                <- tslint configuration
├── typings                    <- typings directory. Contains all the external typing definitions defined with typings
├── typings.json
└── appveyor.yml
```

## Dependencies & Integrations

This is an [Angular 2](https://angular.io/) application built on top of [Nathan Walker's](https://github.com/NathanWalker) [angular2-seed-advanced](https://github.com/NathanWalker/angular2-seed-advanced).

#### Core Tech Stacks
- [Angular 2](https://angular.io/) 2.2
- [Bootstrap 4](http://v4-alpha.getbootstrap.com/) Alpha 4
- [jQuery](http://jquery.com/) 3
- [Chart.js](http://www.chartjs.org/)
- [D3](https://d3js.org/) 3
- [CodeMirror](http://codemirror.net/) 5
- [moment](http://momentjs.com/) 2.14
- [Font Awesome](http://fontawesome.io/) 4.7

#### Integrations
- [ngrx/store](https://github.com/ngrx/store) RxJS powered state management, inspired by **Redux**
- [ngrx/effects](https://github.com/ngrx/effects) Side effect model for @ngrx/store
- [ng2-translate](https://github.com/ocombe/ng2-translate) for i18n
  - Usage is optional but on by default
  - Up to you and your team how you want to utilize it. It can be easily removed if not needed.
- [angulartics2](https://github.com/angulartics/angulartics2) Vendor-agnostic analytics for Angular2 applications.
  - Out of box support for [Segment](https://segment.com/)
    - When using the seed, be sure to change your `write_key` [here](https://github.com/NathanWalker/angular2-seed-advanced/blob/master/src/client/index.html#L24)
  - Can be changed to any vendor, [learn more here](https://github.com/angulartics/angulartics2#supported-providers)
- [lodash](https://lodash.com/) Helps reduce blocks of code down to single lines and enhances readability
- [NativeScript](https://www.nativescript.org/) cross platform mobile (w/ native UI) apps. [Setup instructions here](#nativescript-app).
- [Electron](http://electron.atom.io/) cross platform desktop apps (Mac, Windows and Linux). [Setup instructions here](#electron-app).

#### Sync to the latest seed project changes

Due to the amount of breaking changes introduced in each Angular 2 release, please be super careful when you decide to sync to the latest changes from the [seed project](https://github.com/NathanWalker/angular2-seed-advanced), which will most likely bump up the Angular version and the underlying dependencies.

Last Change Sync'd: 95cec07683d56ad20865f64300bcac824d4009e7 on 11/06/2016

# Cut a Release

Since Xenon UI is independent from the rest of the Xenon, it has its own version and release cycle.

## Release as a web application

This is the default option which let Xenon to host the UI in /core/ui/default.

#### Produce a production build

In the current `ui` directory:

```bash
npm run build.prod
```

It should generate a `prod` folder under `dist`.

#### Clean up the old files

Go to `xenon/xenon-ui/src/main/resources/ui/com/vmware/xenon/ui/UiService`, run

```bash
rm -r *
rm -r .*
```

#### Deploy the new build

In the current `ui` directory:

```bash
cp -a dist/prod/ ../resources/ui/com/vmware/xenon/ui/UiService
```

#### Rebuild Xenon

Run `mvn clean install` under `xenon` directory

## Release as a desktop application

TBD

# Contributing

Please see the [CONTRIBUTING](https://github.com/vmware/xenon/blob/master/CONTRIBUTING.md) file for guidelines.
