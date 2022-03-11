"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[416],{3905:function(e,t,n){n.d(t,{Zo:function(){return u},kt:function(){return f}});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function a(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var g=r.createContext({}),c=function(e){var t=r.useContext(g),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},u=function(e){var t=c(e.components);return r.createElement(g.Provider,{value:t},e.children)},s={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},p=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,i=e.originalType,g=e.parentName,u=a(e,["components","mdxType","originalType","parentName"]),p=c(n),f=o,d=p["".concat(g,".").concat(f)]||p[f]||s[f]||i;return n?r.createElement(d,l(l({ref:t},u),{},{components:n})):r.createElement(d,l({ref:t},u))}));function f(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=n.length,l=new Array(i);l[0]=p;var a={};for(var g in t)hasOwnProperty.call(t,g)&&(a[g]=t[g]);a.originalType=e,a.mdxType="string"==typeof e?e:o,l[1]=a;for(var c=2;c<i;c++)l[c]=n[c];return r.createElement.apply(null,l)}return r.createElement.apply(null,n)}p.displayName="MDXCreateElement"},5580:function(e,t,n){n.r(t),n.d(t,{assets:function(){return u},contentTitle:function(){return g},default:function(){return f},frontMatter:function(){return a},metadata:function(){return c},toc:function(){return s}});var r=n(7462),o=n(3366),i=(n(7294),n(3905)),l=["components"],a={id:"getting-started",title:"Get LoggerF"},g=void 0,c={unversionedId:"monix/getting-started",id:"monix/getting-started",title:"Get LoggerF",description:"Get LoggerF for Monix",source:"@site/../generated-docs/target/mdoc/monix/getting-started.md",sourceDirName:"monix",slug:"/monix/getting-started",permalink:"/docs/monix/getting-started",tags:[],version:"current",frontMatter:{id:"getting-started",title:"Get LoggerF"},sidebar:"theSidebar",previous:{title:"Log - Cats",permalink:"/docs/cats-effect/log"},next:{title:"Log - Monix",permalink:"/docs/monix/log"}},u={},s=[{value:"Get LoggerF for Monix",id:"get-loggerf-for-monix",level:2},{value:"With SLF4J",id:"with-slf4j",level:3},{value:"With Log4j",id:"with-log4j",level:3},{value:"With Log4s",id:"with-log4s",level:3},{value:"With sbt Logging Util",id:"with-sbt-logging-util",level:3},{value:"Log",id:"log",level:3}],p={toc:s};function f(e){var t=e.components,n=(0,o.Z)(e,l);return(0,i.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h2",{id:"get-loggerf-for-monix"},"Get LoggerF for Monix"),(0,i.kt)("p",null,"In ",(0,i.kt)("inlineCode",{parentName:"p"},"build.sbt"),","),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-monix" % "1.20.0"\n')),(0,i.kt)("h3",{id:"with-slf4j"},"With SLF4J"),(0,i.kt)("p",null,"To use ",(0,i.kt)("inlineCode",{parentName:"p"},"logger-f")," with SLF4J, add the following logger"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-slf4j" % "1.20.0"\n')),(0,i.kt)("h3",{id:"with-log4j"},"With Log4j"),(0,i.kt)("p",null,"To use ",(0,i.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4j, add the following logger"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4j" % "1.20.0"\n')),(0,i.kt)("h3",{id:"with-log4s"},"With Log4s"),(0,i.kt)("p",null,"To use ",(0,i.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4s, add the following logger"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4s" % "1.20.0"\n')),(0,i.kt)("h3",{id:"with-sbt-logging-util"},"With sbt Logging Util"),(0,i.kt)("p",null,"You probably need ",(0,i.kt)("inlineCode",{parentName:"p"},"logger-f")," for sbt plugin development."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-sbt-logging" % "1.20.0"\n')),(0,i.kt)("h3",{id:"log"},(0,i.kt)("a",{parentName:"h3",href:"/docs/monix/log"},"Log")))}f.isMDXComponent=!0}}]);