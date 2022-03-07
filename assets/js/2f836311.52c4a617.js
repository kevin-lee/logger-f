"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[150],{3905:function(e,t,n){n.d(t,{Zo:function(){return f},kt:function(){return p}});var r=n(7294);function l(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){l(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,r,l=function(e,t){if(null==e)return{};var n,r,l={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(l[n]=e[n]);return l}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(l[n]=e[n])}return l}var c=r.createContext({}),g=function(e){var t=r.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},f=function(e){var t=g(e.components);return r.createElement(c.Provider,{value:t},e.children)},s={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},u=r.forwardRef((function(e,t){var n=e.components,l=e.mdxType,o=e.originalType,c=e.parentName,f=i(e,["components","mdxType","originalType","parentName"]),u=g(n),p=l,d=u["".concat(c,".").concat(p)]||u[p]||s[p]||o;return n?r.createElement(d,a(a({ref:t},f),{},{components:n})):r.createElement(d,a({ref:t},f))}));function p(e,t){var n=arguments,l=t&&t.mdxType;if("string"==typeof e||l){var o=n.length,a=new Array(o);a[0]=u;var i={};for(var c in t)hasOwnProperty.call(t,c)&&(i[c]=t[c]);i.originalType=e,i.mdxType="string"==typeof e?e:l,a[1]=i;for(var g=2;g<o;g++)a[g]=n[g];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}u.displayName="MDXCreateElement"},1987:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return i},contentTitle:function(){return c},metadata:function(){return g},toc:function(){return f},default:function(){return u}});var r=n(7462),l=n(3366),o=(n(7294),n(3905)),a=["components"],i={id:"getting-started",title:"Get LoggerF"},c=void 0,g={unversionedId:"scalaz-effect/getting-started",id:"scalaz-effect/getting-started",title:"Get LoggerF",description:"Get LoggerF For Scalaz Effect",source:"@site/../generated-docs/target/mdoc/scalaz-effect/getting-started.md",sourceDirName:"scalaz-effect",slug:"/scalaz-effect/getting-started",permalink:"/docs/scalaz-effect/getting-started",tags:[],version:"current",frontMatter:{id:"getting-started",title:"Get LoggerF"},sidebar:"theSidebar",previous:{title:"Log - Monix",permalink:"/docs/monix/log"},next:{title:"Log - Scalaz",permalink:"/docs/scalaz-effect/log"}},f=[{value:"Get LoggerF For Scalaz Effect",id:"get-loggerf-for-scalaz-effect",children:[{value:"With SLF4J",id:"with-slf4j",children:[],level:3},{value:"With Log4j",id:"with-log4j",children:[],level:3},{value:"With Log4s",id:"with-log4s",children:[],level:3},{value:"With sbt Logging Util",id:"with-sbt-logging-util",children:[],level:3}],level:2},{value:"Log",id:"log",children:[],level:2}],s={toc:f};function u(e){var t=e.components,n=(0,l.Z)(e,a);return(0,o.kt)("wrapper",(0,r.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h2",{id:"get-loggerf-for-scalaz-effect"},"Get LoggerF For Scalaz Effect"),(0,o.kt)("p",null,"In ",(0,o.kt)("inlineCode",{parentName:"p"},"build.sbt"),","),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-scalaz-effect" % "1.20.0"\n')),(0,o.kt)("h3",{id:"with-slf4j"},"With SLF4J"),(0,o.kt)("p",null,"To use ",(0,o.kt)("inlineCode",{parentName:"p"},"logger-f")," with SLF4J, add the following logger"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-slf4j" % "1.20.0"\n')),(0,o.kt)("h3",{id:"with-log4j"},"With Log4j"),(0,o.kt)("p",null,"To use ",(0,o.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4j, add the following logger"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4j" % "1.20.0"\n')),(0,o.kt)("h3",{id:"with-log4s"},"With Log4s"),(0,o.kt)("p",null,"To use ",(0,o.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4s, add the following logger"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4s" % "1.20.0"\n')),(0,o.kt)("h3",{id:"with-sbt-logging-util"},"With sbt Logging Util"),(0,o.kt)("p",null,"You probably need ",(0,o.kt)("inlineCode",{parentName:"p"},"logger-f")," for sbt plugin development."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-sbt-logging" % "1.20.0"\n')),(0,o.kt)("h2",{id:"log"},(0,o.kt)("a",{parentName:"h2",href:"/docs/scalaz-effect/log"},"Log")))}u.isMDXComponent=!0}}]);