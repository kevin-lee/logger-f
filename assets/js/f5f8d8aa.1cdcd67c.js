"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[396],{3905:function(e,t,n){n.d(t,{Zo:function(){return s},kt:function(){return p}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function l(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?l(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):l(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},l=Object.keys(e);for(r=0;r<l.length;r++)n=l[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(r=0;r<l.length;r++)n=l[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var u=r.createContext({}),c=function(e){var t=r.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},s=function(e){var t=c(e.components);return r.createElement(u.Provider,{value:t},e.children)},f={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},g=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,l=e.originalType,u=e.parentName,s=i(e,["components","mdxType","originalType","parentName"]),g=c(n),p=a,d=g["".concat(u,".").concat(p)]||g[p]||f[p]||l;return n?r.createElement(d,o(o({ref:t},s),{},{components:n})):r.createElement(d,o({ref:t},s))}));function p(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var l=n.length,o=new Array(l);o[0]=g;var i={};for(var u in t)hasOwnProperty.call(t,u)&&(i[u]=t[u]);i.originalType=e,i.mdxType="string"==typeof e?e:a,o[1]=i;for(var c=2;c<l;c++)o[c]=n[c];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}g.displayName="MDXCreateElement"},8215:function(e,t,n){var r=n(7294);t.Z=function(e){var t=e.children,n=e.hidden,a=e.className;return r.createElement("div",{role:"tabpanel",hidden:n,className:a},t)}},6396:function(e,t,n){n.d(t,{Z:function(){return g}});var r=n(7462),a=n(7294),l=n(2389),o=n(9443);var i=function(){var e=(0,a.useContext)(o.Z);if(null==e)throw new Error('"useUserPreferencesContext" is used outside of "Layout" component.');return e},u=n(3616),c=n(6010),s="tabItem_vU9c";function f(e){var t,n,l,o=e.lazy,f=e.block,g=e.defaultValue,p=e.values,d=e.groupId,v=e.className,m=a.Children.map(e.children,(function(e){if((0,a.isValidElement)(e)&&void 0!==e.props.value)return e;throw new Error("Docusaurus error: Bad <Tabs> child <"+("string"==typeof e.type?e.type:e.type.name)+'>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.')})),h=null!=p?p:m.map((function(e){var t=e.props;return{value:t.value,label:t.label,attributes:t.attributes}})),b=(0,u.lx)(h,(function(e,t){return e.value===t.value}));if(b.length>0)throw new Error('Docusaurus error: Duplicate values "'+b.map((function(e){return e.value})).join(", ")+'" found in <Tabs>. Every value needs to be unique.');var k=null===g?g:null!=(t=null!=g?g:null==(n=m.find((function(e){return e.props.default})))?void 0:n.props.value)?t:null==(l=m[0])?void 0:l.props.value;if(null!==k&&!h.some((function(e){return e.value===k})))throw new Error('Docusaurus error: The <Tabs> has a defaultValue "'+k+'" but none of its children has the corresponding value. Available values are: '+h.map((function(e){return e.value})).join(", ")+". If you intend to show no default tab, use defaultValue={null} instead.");var y=i(),w=y.tabGroupChoices,N=y.setTabGroupChoices,E=(0,a.useState)(k),O=E[0],T=E[1],j=[],C=(0,u.o5)().blockElementScrollPositionUntilNextRender;if(null!=d){var L=w[d];null!=L&&L!==O&&h.some((function(e){return e.value===L}))&&T(L)}var x=function(e){var t=e.currentTarget,n=j.indexOf(t),r=h[n].value;r!==O&&(C(t),T(r),null!=d&&N(d,r))},P=function(e){var t,n=null;switch(e.key){case"ArrowRight":var r=j.indexOf(e.currentTarget)+1;n=j[r]||j[0];break;case"ArrowLeft":var a=j.indexOf(e.currentTarget)-1;n=j[a]||j[j.length-1]}null==(t=n)||t.focus()};return a.createElement("div",{className:"tabs-container"},a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,c.Z)("tabs",{"tabs--block":f},v)},h.map((function(e){var t=e.value,n=e.label,l=e.attributes;return a.createElement("li",(0,r.Z)({role:"tab",tabIndex:O===t?0:-1,"aria-selected":O===t,key:t,ref:function(e){return j.push(e)},onKeyDown:P,onFocus:x,onClick:x},l,{className:(0,c.Z)("tabs__item",s,null==l?void 0:l.className,{"tabs__item--active":O===t})}),null!=n?n:t)}))),o?(0,a.cloneElement)(m.filter((function(e){return e.props.value===O}))[0],{className:"margin-vert--md"}):a.createElement("div",{className:"margin-vert--md"},m.map((function(e,t){return(0,a.cloneElement)(e,{key:t,hidden:e.props.value!==O})}))))}function g(e){var t=(0,l.Z)();return a.createElement(f,(0,r.Z)({key:String(t)},e))}},4603:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return c},contentTitle:function(){return s},metadata:function(){return f},toc:function(){return g},default:function(){return d}});var r=n(7462),a=n(3366),l=(n(7294),n(3905)),o=n(6396),i=n(8215),u=["components"],c={id:"getting-started",title:"Get LoggerF"},s=void 0,f={unversionedId:"cats-effect/getting-started",id:"cats-effect/getting-started",title:"Get LoggerF",description:"Get LoggerF for Cats Effect",source:"@site/../generated-docs/target/mdoc/cats-effect/getting-started.md",sourceDirName:"cats-effect",slug:"/cats-effect/getting-started",permalink:"/docs/cats-effect/getting-started",tags:[],version:"current",frontMatter:{id:"getting-started",title:"Get LoggerF"},sidebar:"theSidebar",previous:{title:"Getting Started",permalink:"/docs/"},next:{title:"Log - Cats",permalink:"/docs/cats-effect/log"}},g=[{value:"Get LoggerF for Cats Effect",id:"get-loggerf-for-cats-effect",children:[{value:"With SLF4J",id:"with-slf4j",children:[],level:3},{value:"With Log4j",id:"with-log4j",children:[],level:3},{value:"With Log4s",id:"with-log4s",children:[],level:3},{value:"With sbt Logging Util",id:"with-sbt-logging-util",children:[],level:3}],level:2},{value:"Log",id:"log",children:[],level:2}],p={toc:g};function d(e){var t=e.components,n=(0,a.Z)(e,u);return(0,l.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,l.kt)("h2",{id:"get-loggerf-for-cats-effect"},"Get LoggerF for Cats Effect"),(0,l.kt)("p",null,"In ",(0,l.kt)("inlineCode",{parentName:"p"},"build.sbt"),","),(0,l.kt)(o.Z,{groupId:"cats-effect",defaultValue:"cats-effect",values:[{label:"Cats Effect 3",value:"cats-effect3"},{label:"Cats Effect 2",value:"cats-effect"}],mdxType:"Tabs"},(0,l.kt)(i.Z,{value:"cats-effect3",mdxType:"TabItem"},(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-cats-effect3" % "1.20.0"\n'))),(0,l.kt)(i.Z,{value:"cats-effect",mdxType:"TabItem"},(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-cats-effect" % "1.20.0"\n')))),(0,l.kt)("h3",{id:"with-slf4j"},"With SLF4J"),(0,l.kt)("p",null,"To use ",(0,l.kt)("inlineCode",{parentName:"p"},"logger-f")," with SLF4J, add the following logger"),(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-slf4j" % "1.20.0"\n')),(0,l.kt)("h3",{id:"with-log4j"},"With Log4j"),(0,l.kt)("p",null,"To use ",(0,l.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4j, add the following logger"),(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4j" % "1.20.0"\n')),(0,l.kt)("h3",{id:"with-log4s"},"With Log4s"),(0,l.kt)("p",null,"To use ",(0,l.kt)("inlineCode",{parentName:"p"},"logger-f")," with Log4s, add the following logger"),(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-log4s" % "1.20.0"\n')),(0,l.kt)("h3",{id:"with-sbt-logging-util"},"With sbt Logging Util"),(0,l.kt)("p",null,"You probably need ",(0,l.kt)("inlineCode",{parentName:"p"},"logger-f")," for sbt plugin development."),(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-scala"},'"io.kevinlee" %% "logger-f-sbt-logging" % "1.20.0"\n')),(0,l.kt)("h2",{id:"log"},(0,l.kt)("a",{parentName:"h2",href:"/docs/cats-effect/log"},"Log")))}d.isMDXComponent=!0}}]);