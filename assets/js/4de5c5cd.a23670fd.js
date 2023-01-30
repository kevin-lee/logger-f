"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[478],{3905:(e,t,n)=>{n.d(t,{Zo:()=>c,kt:()=>d});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var g=a.createContext({}),s=function(e){var t=a.useContext(g),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},c=function(e){var t=s(e.components);return a.createElement(g.Provider,{value:t},e.children)},p="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},f=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,o=e.originalType,g=e.parentName,c=i(e,["components","mdxType","originalType","parentName"]),p=s(n),f=r,d=p["".concat(g,".").concat(f)]||p[f]||u[f]||o;return n?a.createElement(d,l(l({ref:t},c),{},{components:n})):a.createElement(d,l({ref:t},c))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var o=n.length,l=new Array(o);l[0]=f;var i={};for(var g in t)hasOwnProperty.call(t,g)&&(i[g]=t[g]);i.originalType=e,i[p]="string"==typeof e?e:r,l[1]=i;for(var s=2;s<o;s++)l[s]=n[s];return a.createElement.apply(null,l)}return a.createElement.apply(null,n)}f.displayName="MDXCreateElement"},5162:(e,t,n)=>{n.d(t,{Z:()=>l});var a=n(7294),r=n(6010);const o="tabItem_Ymn6";function l(e){let{children:t,hidden:n,className:l}=e;return a.createElement("div",{role:"tabpanel",className:(0,r.Z)(o,l),hidden:n},t)}},4866:(e,t,n)=>{n.d(t,{Z:()=>N});var a=n(7462),r=n(7294),o=n(6010),l=n(2466),i=n(6550),g=n(1980),s=n(7392),c=n(12);function p(e){return function(e){return r.Children.map(e,(e=>{if((0,r.isValidElement)(e)&&"value"in e.props)return e;throw new Error(`Docusaurus error: Bad <Tabs> child <${"string"==typeof e.type?e.type:e.type.name}>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.`)}))}(e).map((e=>{let{props:{value:t,label:n,attributes:a,default:r}}=e;return{value:t,label:n,attributes:a,default:r}}))}function u(e){const{values:t,children:n}=e;return(0,r.useMemo)((()=>{const e=t??p(n);return function(e){const t=(0,s.l)(e,((e,t)=>e.value===t.value));if(t.length>0)throw new Error(`Docusaurus error: Duplicate values "${t.map((e=>e.value)).join(", ")}" found in <Tabs>. Every value needs to be unique.`)}(e),e}),[t,n])}function f(e){let{value:t,tabValues:n}=e;return n.some((e=>e.value===t))}function d(e){let{queryString:t=!1,groupId:n}=e;const a=(0,i.k6)(),o=function(e){let{queryString:t=!1,groupId:n}=e;if("string"==typeof t)return t;if(!1===t)return null;if(!0===t&&!n)throw new Error('Docusaurus error: The <Tabs> component groupId prop is required if queryString=true, because this value is used as the search param name. You can also provide an explicit value such as queryString="my-search-param".');return n??null}({queryString:t,groupId:n});return[(0,g._X)(o),(0,r.useCallback)((e=>{if(!o)return;const t=new URLSearchParams(a.location.search);t.set(o,e),a.replace({...a.location,search:t.toString()})}),[o,a])]}function m(e){const{defaultValue:t,queryString:n=!1,groupId:a}=e,o=u(e),[l,i]=(0,r.useState)((()=>function(e){let{defaultValue:t,tabValues:n}=e;if(0===n.length)throw new Error("Docusaurus error: the <Tabs> component requires at least one <TabItem> children component");if(t){if(!f({value:t,tabValues:n}))throw new Error(`Docusaurus error: The <Tabs> has a defaultValue "${t}" but none of its children has the corresponding value. Available values are: ${n.map((e=>e.value)).join(", ")}. If you intend to show no default tab, use defaultValue={null} instead.`);return t}const a=n.find((e=>e.default))??n[0];if(!a)throw new Error("Unexpected error: 0 tabValues");return a.value}({defaultValue:t,tabValues:o}))),[g,s]=d({queryString:n,groupId:a}),[p,m]=function(e){let{groupId:t}=e;const n=function(e){return e?`docusaurus.tab.${e}`:null}(t),[a,o]=(0,c.Nk)(n);return[a,(0,r.useCallback)((e=>{n&&o.set(e)}),[n,o])]}({groupId:a}),A=(()=>{const e=g??p;return f({value:e,tabValues:o})?e:null})();(0,r.useEffect)((()=>{A&&i(A)}),[A]);return{selectedValue:l,selectValue:(0,r.useCallback)((e=>{if(!f({value:e,tabValues:o}))throw new Error(`Can't select invalid tab value=${e}`);i(e),s(e),m(e)}),[s,m,o]),tabValues:o}}var A=n(2389);const h="tabList__CuJ",b="tabItem_LNqP";function k(e){let{className:t,block:n,selectedValue:i,selectValue:g,tabValues:s}=e;const c=[],{blockElementScrollPositionUntilNextRender:p}=(0,l.o5)(),u=e=>{const t=e.currentTarget,n=c.indexOf(t),a=s[n].value;a!==i&&(p(t),g(a))},f=e=>{let t=null;switch(e.key){case"Enter":u(e);break;case"ArrowRight":{const n=c.indexOf(e.currentTarget)+1;t=c[n]??c[0];break}case"ArrowLeft":{const n=c.indexOf(e.currentTarget)-1;t=c[n]??c[c.length-1];break}}t?.focus()};return r.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,o.Z)("tabs",{"tabs--block":n},t)},s.map((e=>{let{value:t,label:n,attributes:l}=e;return r.createElement("li",(0,a.Z)({role:"tab",tabIndex:i===t?0:-1,"aria-selected":i===t,key:t,ref:e=>c.push(e),onKeyDown:f,onClick:u},l,{className:(0,o.Z)("tabs__item",b,l?.className,{"tabs__item--active":i===t})}),n??t)})))}function v(e){let{lazy:t,children:n,selectedValue:a}=e;if(t){const e=n.find((e=>e.props.value===a));return e?(0,r.cloneElement)(e,{className:"margin-top--md"}):null}return r.createElement("div",{className:"margin-top--md"},n.map(((e,t)=>(0,r.cloneElement)(e,{key:t,hidden:e.props.value!==a}))))}function y(e){const t=m(e);return r.createElement("div",{className:(0,o.Z)("tabs-container",h)},r.createElement(k,(0,a.Z)({},e,t)),r.createElement(v,(0,a.Z)({},e,t)))}function N(e){const t=(0,A.Z)();return r.createElement(y,(0,a.Z)({key:String(t)},e))}},5317:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>g,contentTitle:()=>l,default:()=>p,frontMatter:()=>o,metadata:()=>i,toc:()=>s});var a=n(7462),r=(n(7294),n(3905));n(4866),n(5162);const o={sidebar_position:1,id:"getting-started",title:"Getting Started",slug:"/"},l=void 0,i={unversionedId:"getting-started",id:"getting-started",title:"Getting Started",description:"Build Status",source:"@site/../generated-docs/docs/getting-started.md",sourceDirName:".",slug:"/",permalink:"/docs/",draft:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1,id:"getting-started",title:"Getting Started",slug:"/"},sidebar:"theSidebar",next:{title:"Get LoggerF",permalink:"/docs/cats/"}},g={},s=[{value:" LoggerF - Logger for <code>F[_]</code>",id:"-loggerf---logger-for-f_",level:2},{value:"Getting Started",id:"getting-started",level:2},{value:"Get LoggerF For Cats Effect",id:"get-loggerf-for-cats-effect",level:3},{value:"Get LoggerF For Cats",id:"get-loggerf-for-cats",level:3},{value:"With SLF4J",id:"with-slf4j",level:4},{value:"With Log4j",id:"with-log4j",level:4},{value:"With Log4s",id:"with-log4s",level:4},{value:"With sbt Logging Util",id:"with-sbt-logging-util",level:4},{value:"Why",id:"why",level:2},{value:"Usage",id:"usage",level:3}],c={toc:s};function p(e){let{components:t,...o}=e;return(0,r.kt)("wrapper",(0,a.Z)({},c,o,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,(0,r.kt)("a",{parentName:"p",href:"https://github.com/Kevin-Lee/logger-f/actions?workflow=Build-All"},(0,r.kt)("img",{parentName:"a",src:"https://github.com/Kevin-Lee/logger-f/workflows/Build-All/badge.svg",alt:"Build Status"})),"\n",(0,r.kt)("a",{parentName:"p",href:"https://github.com/Kevin-Lee/logger-f/actions?workflow=Release"},(0,r.kt)("img",{parentName:"a",src:"https://github.com/Kevin-Lee/logger-f/workflows/Release/badge.svg",alt:"Release Status"})),"\n",(0,r.kt)("a",{parentName:"p",href:"https://index.scala-lang.org/kevin-lee/logger-f"},(0,r.kt)("img",{parentName:"a",src:"https://index.scala-lang.org/kevin-lee/logger-f/latest.svg",alt:"Latest version"}))),(0,r.kt)("table",null,(0,r.kt)("thead",{parentName:"table"},(0,r.kt)("tr",{parentName:"thead"},(0,r.kt)("th",{parentName:"tr",align:"right"},"Project"),(0,r.kt)("th",{parentName:"tr",align:null},"Maven Central"))),(0,r.kt)("tbody",{parentName:"table"},(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:"right"},"logger-f-cats"),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("a",{parentName:"td",href:"https://search.maven.org/artifact/io.kevinlee/logger-f-cats_2.13"},(0,r.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-cats_2.13/badge.svg",alt:"Maven Central"})))),(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:"right"},"logger-f-slf4j"),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("a",{parentName:"td",href:"https://search.maven.org/artifact/io.kevinlee/logger-f-slf4j_2.13"},(0,r.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-slf4j_2.13/badge.svg",alt:"Maven Central"})))),(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:"right"},"logger-f-log4j"),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("a",{parentName:"td",href:"https://search.maven.org/artifact/io.kevinlee/logger-f-log4j_2.13"},(0,r.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-log4j_2.13/badge.svg",alt:"Maven Central"})))),(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:"right"},"logger-f-log4s"),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("a",{parentName:"td",href:"https://search.maven.org/artifact/io.kevinlee/logger-f-log4s_2.13"},(0,r.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-log4s_2.13/badge.svg",alt:"Maven Central"})))),(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:"right"},"logger-f-sbt-logging"),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("a",{parentName:"td",href:"https://search.maven.org/artifact/io.kevinlee/logger-f-sbt-logging_2.13"},(0,r.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/logger-f-sbt-logging_2.13/badge.svg",alt:"Maven Central"})))))),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Supported Scala Versions: ",(0,r.kt)("inlineCode",{parentName:"li"},"3"),", ",(0,r.kt)("inlineCode",{parentName:"li"},"2.13")," and ",(0,r.kt)("inlineCode",{parentName:"li"},"2.12"))),(0,r.kt)("h2",{id:"-loggerf---logger-for-f_"},(0,r.kt)("img",{src:n(7759).Z,width:"96",height:"96"})," LoggerF - Logger for ",(0,r.kt)("inlineCode",{parentName:"h2"},"F[_]")),(0,r.kt)("p",null,"LoggerF is a tool for logging tagless final with an effect library. LoggerF requires ",(0,r.kt)("a",{parentName:"p",href:"https://kevin-lee.github.io/effectie"},"Effectie")," to construct ",(0,r.kt)("inlineCode",{parentName:"p"},"F[_]"),". All the example code in this doc site uses Effectie so if you're not familiar with it, please check out ",(0,r.kt)("a",{parentName:"p",href:"https://kevin-lee.github.io/effectie"},"Effectie")," website."),(0,r.kt)("p",null,"Why LoggerF? Why not just log with ",(0,r.kt)("inlineCode",{parentName:"p"},"map")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"flatMap"),"? Please read ",(0,r.kt)("a",{parentName:"p",href:"#why"},'"Why?"')," section."),(0,r.kt)("h2",{id:"getting-started"},"Getting Started"),(0,r.kt)("h3",{id:"get-loggerf-for-cats-effect"},"Get LoggerF For Cats Effect"),(0,r.kt)("h3",{id:"get-loggerf-for-cats"},"Get LoggerF For Cats"),(0,r.kt)("p",null,"logger-f can be used wit any effect library or ",(0,r.kt)("inlineCode",{parentName:"p"},"Future")," as long as there is an instance of ",(0,r.kt)("inlineCode",{parentName:"p"},"Fx")," from effectie."),(0,r.kt)("h4",{id:"with-slf4j"},"With SLF4J"),(0,r.kt)("p",null,"In ",(0,r.kt)("inlineCode",{parentName:"p"},"build.sbt"),","),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'libraryDependencies ++=\n  Seq(\n    "io.kevinlee" %% "logger-f-cats" % "2.0.0-beta6",\n    "io.kevinlee" %% "logger-f-slf4j" % "2.0.0-beta6"\n  )\n')),(0,r.kt)("h4",{id:"with-log4j"},"With Log4j"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'libraryDependencies ++=\n  Seq(\n    "io.kevinlee" %% "logger-f-cats" % "2.0.0-beta6",\n    "io.kevinlee" %% "logger-f-log4j" % "2.0.0-beta6"\n  )\n')),(0,r.kt)("h4",{id:"with-log4s"},"With Log4s"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'libraryDependencies ++=\n  Seq(\n    "io.kevinlee" %% "logger-f-cats" % "2.0.0-beta6",\n    "io.kevinlee" %% "logger-f-log4s" % "2.0.0-beta6"\n  )\n')),(0,r.kt)("h4",{id:"with-sbt-logging-util"},"With sbt Logging Util"),(0,r.kt)("p",null,"You probably need ",(0,r.kt)("inlineCode",{parentName:"p"},"logger-f")," for sbt plugin development."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'libraryDependencies ++=\n  Seq(\n    "io.kevinlee" %% "logger-f-cats" % "2.0.0-beta6",\n    "io.kevinlee" %% "logger-f-sbt-logging" % "2.0.0-beta6"\n  )\n')),(0,r.kt)("h2",{id:"why"},"Why"),(0,r.kt)("p",null,"If you code tagless final and use some effect library like ",(0,r.kt)("a",{parentName:"p",href:"https://typelevel.org/cats-effect"},"Cats Effect")," and ",(0,r.kt)("a",{parentName:"p",href:"https://monix.io"},"Monix")," or use ",(0,r.kt)("inlineCode",{parentName:"p"},"Future"),", you may have inconvenience in logging."),(0,r.kt)("p",null,"What inconvenience? I can just log with ",(0,r.kt)("inlineCode",{parentName:"p"},"flatMap")," like."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'for {\n  a <- foo(n) // F[A]\n  _ <- Sync[F].delay(logger.debug(s"a is $a")) // F[Unit]\n  b <- bar(a) // F[A]\n  _ <- Sync[F].delay(logger.debug(s"b is $b")) // F[Unit]\n} yield b\n')),(0,r.kt)("p",null,"That's true, but it's distracting to have log in each flatMap.\nSo,"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"1 line for the actual code\n1 line for logging\n1 line for code\n1 line for the actual logging\n")),(0,r.kt)("p",null,"Also, what about ",(0,r.kt)("inlineCode",{parentName:"p"},"F[_]")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"Option")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"Either"),"? What happens if you want to use ",(0,r.kt)("inlineCode",{parentName:"p"},"Option")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"Either"),"?\nIf you use ",(0,r.kt)("inlineCode",{parentName:"p"},"F[_]")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"Option")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"Either"),", you may have more inconvenience or may not get the result you want."),(0,r.kt)("p",null,"e.g.)"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.syntax.all._\nimport cats.effect._\n\nimport org.slf4j.LoggerFactory\nval logger = LoggerFactory.getLogger("test-logger")\n// logger: org.slf4j.Logger = Logger[test-logger]\n\ndef foo[F[_]: Sync](n: Int): F[Option[Int]] = for {\n  a <- Sync[F].pure(n.some)\n  _ <- Sync[F].delay(\n         a match {\n           case Some(value) =>\n             logger.debug(s"a is $value")\n           case None =>\n             logger.debug("No \'a\' value found")\n         }\n       ) // F[Unit]\n  b <- Sync[F].pure(none[Int])\n  _ <- Sync[F].delay(\n         b match {\n           case Some(value) =>\n             logger.debug(s"b is $value")\n           case None =>\n             () // don\'t log anything for None case\n         }\n       ) // F[Unit]\n  c <- Sync[F].pure(123.some)\n  _ <- Sync[F].delay(\n         c match {\n           case Some(value) =>\n             () // don\'t log anything for None case\n           case None =>\n             logger.debug("No \'c\' value found")\n         }\n       ) // F[Unit]\n} yield c\n')),(0,r.kt)("p",null,"So much noise for logging!"),(0,r.kt)("p",null,"Now, let's think about the result."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"foo[IO](1).unsafeRunSync() // You probably want to have None here.\n// res1: Option[Int] = Some(value = 123)\n")),(0,r.kt)("p",null,"You expect ",(0,r.kt)("inlineCode",{parentName:"p"},"None")," for the result due to ",(0,r.kt)("inlineCode",{parentName:"p"},"Sync[F].pure(none[Int])")," yet you get ",(0,r.kt)("inlineCode",{parentName:"p"},"Some(123)")," instead. That's because ",(0,r.kt)("inlineCode",{parentName:"p"},"b")," is from ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Option[Int]]")," not from ",(0,r.kt)("inlineCode",{parentName:"p"},"Option[Int]"),"."),(0,r.kt)("p",null,"The same issue exists for ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]")," as well."),(0,r.kt)("p",null,"So you need to use ",(0,r.kt)("inlineCode",{parentName:"p"},"OptionT")," for ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Option[A]]")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"EitherT")," for ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),"."),(0,r.kt)("p",null,"Let's write it again with ",(0,r.kt)("inlineCode",{parentName:"p"},"OptionT"),"."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.data._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport org.slf4j.LoggerFactory\nval logger = LoggerFactory.getLogger("test-logger")\n// logger: org.slf4j.Logger = Logger[test-logger]\n\ndef foo[F[_]: Sync](n: Int): F[Option[Int]] = (for {\n  a <- OptionT(Sync[F].pure(n.some))\n  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"a is $a"))) // Now, you can\'t log None case.\n  b <- OptionT(Sync[F].pure(none[Int]))\n  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"b is $b"))) // You can\'t log None case.\n  c <- OptionT(Sync[F].pure(123.some))\n  _ <- OptionT.liftF(Sync[F].delay(logger.debug(s"c is $c"))) // You can\'t log None case.\n} yield c).value\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"foo[IO](1).unsafeRunSync() // You expect None here.\n// res3: Option[Int] = None\n")),(0,r.kt)("p",null,"The problem's gone! Now each ",(0,r.kt)("inlineCode",{parentName:"p"},"flatMap")," handles only ",(0,r.kt)("inlineCode",{parentName:"p"},"Some")," case and that's what you want. However, because of that, it's hard to log ",(0,r.kt)("inlineCode",{parentName:"p"},"None")," case."),(0,r.kt)("hr",null),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"LoggerF can solve this issue for you!")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.data._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef foo[F[_]: Fx: Monad: Log](n: Int): F[Option[Int]] =\n  (for {\n    a <- OptionT(effectOf(n.some)).log(\n           ifEmpty = error("a is empty"),\n           a => debug(s"a is $a")\n         )\n    b <- OptionT(effectOf(none[Int])).log(\n           error("b is empty"),\n           b => debug(s"b is $b")\n         )\n    c <- OptionT(effectOf(123.some)).log(\n           warn("c is empty"),\n           c => debug(s"c is $c")\n         )\n  } yield c).value\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import loggerf.logger._\n\n// or Slf4JLogger.slf4JLogger[MyClass]\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyLogger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@132e7365\n\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\n\nfoo[IO](1).unsafeRunSync() // You expect None here.\n// res5: Option[Int] = None\n')),(0,r.kt)("p",null,"With logs like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"00:17:33.983 [main] DEBUG MyLogger - a is 1\n00:17:33.995 [main] ERROR MyLogger - b is empty\n")),(0,r.kt)("hr",null),(0,r.kt)("p",null,"Another example with ",(0,r.kt)("inlineCode",{parentName:"p"},"EitherT"),","),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.data._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\n// or Slf4JLogger.slf4JLogger[MyClass]\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyLogger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@750d3dfb\n\ndef foo[F[_] : Fx : Monad : Log](n: Int): F[Either[String, Int]] =\n  (for {\n    a <- EitherT(effectOf(n.asRight[String])).log(\n           err => error(s"Error: $err"),\n           a => debug(s"a is $a")\n         )\n    b <- EitherT(effectOf("Some Error".asLeft[Int])).log(\n           err => error(s"Error: $err"),\n            b => debug(s"b is $b")\n         )\n    c <- EitherT(effectOf(123.asRight[String])).log(\n           err => warn(s"Error: $err"),\n           c => debug(s"c is $c")\n         )\n  } yield c).value\n\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\n\nfoo[IO](1).unsafeRunSync() // You expect Left("Some Error") here.\n// res7: Either[String, Int] = Left(value = "Some Error")\n')),(0,r.kt)("p",null,"With logs like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"00:40:48.663 [main] DEBUG MyLogger - a is 1\n00:40:48.667 [main] ERROR MyLogger - Error: Some Error\n")),(0,r.kt)("h3",{id:"usage"},"Usage"),(0,r.kt)("p",null,"Pleae check out"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"/docs/cats/"},"LoggerF for Cats"))))}p.isMDXComponent=!0},7759:(e,t,n)=>{n.d(t,{Z:()=>a});const a="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGAAAABgCAYAAADimHc4AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAhGVYSWZNTQAqAAAACAAFARIAAwAAAAEAAQAAARoABQAAAAEAAABKARsABQAAAAEAAABSASgAAwAAAAEAAgAAh2kABAAAAAEAAABaAAAAAAAAAEgAAAABAAAASAAAAAEAA6ABAAMAAAABAAEAAKACAAQAAAABAAAAYKADAAQAAAABAAAAYAAAAABaCTJNAAAACXBIWXMAAAsTAAALEwEAmpwYAAACNGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNS40LjAiPgogICA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPgogICAgICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgICAgICAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgICAgICAgICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8ZXhpZjpDb2xvclNwYWNlPjE8L2V4aWY6Q29sb3JTcGFjZT4KICAgICAgICAgPGV4aWY6UGl4ZWxYRGltZW5zaW9uPjE5MjA8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MTkyMDwvZXhpZjpQaXhlbFlEaW1lbnNpb24+CiAgICAgIDwvcmRmOkRlc2NyaXB0aW9uPgogICA8L3JkZjpSREY+CjwveDp4bXBtZXRhPgqsc70BAAAjmElEQVR4Ae19B5xV1bX32uecW2eYPsPQZ+iIgjTpCEIAxS5iQSWJRn0GNS/6fIp+cXxPMRrLp+bFyGdsSSxgbAgJBKSEIkiRjgLC0IYyvd52zn7/te89M3eGGcLM3GHQL/v3u/fUvc9qe6211177HKJ/lX9R4P9nCojvNfJShuFX/0J+H3Exvm9A50ipHZ1DelEyWXOJLKZ9jiRBy6XOuOSME6HvG07fG3hz5krnPwP2tQ3SkZMjtX9237ly/XvTA3KWSQPSHWDCPfa5nCAtGofdbOgdZspxKWk9xdHndw0WBXwP95QcISzeP5dLWIeeyxACtuvnSn3eNGE+8qkcKQS9aDhpiNNNpJQ+/nCOLJPIV0EncfKZ2VeJ5xmluag3DfXOZfTOeQZEJD806zP5Y92gN0F88lUSW98gCKt4ECGwrjvIcHmJKkvp49lXiGv5vM28yD3n3OacZoBN/Ic/lZPdHvprCArIkuQD2Vnt1NLz4IRETwjhmhWfQq6KQnrvqSvFzcSeksLy3PSSzlkGSBAORebMl96ApJ1OD3UJ+BTxoXwaLooR6B3eRHJWlNBtT18p/mgzsuFarXellhTFBgxIHEtdc34A5InlpNzKoKRbQMwugSryQ7rrEp9VULQaYmFnodKCfrUzi3GqcU2bCRfjpLQftxqbEhsGALA7X5MO1rfwRhRFbMo0ZatQG0u2B3ONBc8e7daFlf19vocfEYo8VlXFnxH0kQl70fuRz+VoPsm9ACxh8JoFXw7GHNwW48rtNrc02w1ln3vHPBJz7hJsFMPqtrlQoT67kPculC4Zoj4htAz9Xs0AENF0OMkwHOAAWCA00vwVzCMlnrZalWywYTcGo7l/FFSpHhVitdZM8Li+Eg4WOhvvprbZLAYw8XNywr72o/PlRaDAlSBUFnz0GulgstmyzFDaZLTPRa6jHndwgXsNU9J/PXO12BZPlABqxqE9LoqwTHy4oDokfC+I+zLE+SSuXWe4aCob6TpM4BPpXDnVQ+asT+VsMKs34KxCa1o1XNEw1oWPK6NUw0dUhMNVe6rogznTRLC59qXJDIgYSUWaR+bLVzSDZsJQhvu3LYMMOH5cbLGr71hdwx+QJLRD5YX0Bqpsc0kQWtWu/gux5MMYHySDRjx9mWDic3kfAvAKXNCZ/kpiv78GL5ugY0Hu+XRDQgZ1RW8hDeejYYre5wajj+19Ps8FAvFvPYkeBt7TYF92N4cJNYCG2z7j/2nzlCyzVL3aJpXuLssnWRWAbw5JjjRi07rhNvlOvou3LI9hPe8AbWBCwwWXarXDasUM0FtPgfgPLpJxmo+0Z68SZaZFL4GwdwmdHFBLbC1VgcJR8LCa7OmmE4CzKwZtVbionxGkNow1PGGINE88XVBVRitzFsh+YMKxpo43bPmIgHtmG+Y4j0whdaOcXkX8ELq1BcDYP3epX9j7VsifWaun3gVJZzratKy+gdVB9UFkB/aAQ3KnnMc5W9nVc7FuK1HHdVrCoRNthXGTZFSWURW8s/SASU9zreSiauUa1cg/321aDzgZfhi64o0atD2Iz4Og6LZCDhc5oC7OHGlgGKWCGFEKGSQMkJ9pEbnMRpUHYzMe+Ui+8vSkcNyH75Um3QcV5IQKCqFKDSwsGDUlA72VcI8HtuCMCnOfDb2vnHeUkHFNBtWF0AeXS3MWyoScy0RpU0IfNYCqts7s7zxoAXWnRp3YQAIahpML04pgCwz47bshwVtwLYxqRBXw9epSU49rsgQrI4zrR/geg8e1Dpxj6ofbNoIBZYSz4OevefQz+RKusThchzDFDSAsl1qkBXgKViWh7WheyQk6D81VVsPFNZhFdXtPGBaGnhmaBoZNgN3QzGA1E9gWcEnyS8rEtrSoq3p2mDbq0j//axID5jXQLhCzXPBQQPhXZ18p7mngtjM+HRAgtqJ9TRUQg9s30cN6guj/owQAJI+4ocyoWgwAoZUdb9+TdOjqh2taatzeY/PlRDNEn8DGePBMRXo8i2WDcJ53m1RqAdukFmoebTkcijiHnPF0X31thd1Wdl3PLF4PxMrqaweP1EN+CoHoJnqahS0PxJgKNdDwcfiolNsA8X28PV1hFQKSoi9U23B1O/v7T14hFoPav+VgHwq0IEZ1ahfAGIoPkaPGbZrUA2o9AlAwMMBVsgtJAToAZNWsFIz0FZCW20AWn67TszlTxDbbW4ALNxXcvwz2Q5EJDdQiHhoNgQxx2HojXb329bCet2nApqJu0cAcLrfCUzsf/cKLtiz7OUrrQO2hAR2U3OdMoJemjRPFTPzr4eH1mC8fR93zMcrZUemn2dhnD28nI8sl6nmiOT2g+QwIw1Pzb+t8nIGYDEvrRFNZN1cU0Vyc2hbxnn4JD+J5juFziUhq+MD+B4ZMeLh6jDOjHYWzfVO95+yLGghjOdw01OmioUzp+gobWB6/lJykKXBrxz0nREXya+ihbro1I4uyThygwYnp9BTX1QQsUoxL7BkQBSAoVlqKoRJGrezjw38heuhT2QbE+CXOEXQ5qxj2eBogDwjMSEd6CddvZNHwnBDbjPq4x23xg+HhWPFJNKSimKbgcG77ZOKptxNgShaOj+OnCsYaMS8tygBAa/DACZLO0qjsjdeguKBJDhxz4bHz6WFoiDWq+hn9sXpq8Bmq90E44N1wT0zhFgviSSSEyMGxJowIgUHLleYb4dPABtcNEQtIWRQRA3AtlQVr0SefBqi6l8LwCR4XQB2d4kKCKVHQ163c/OMGJaP5TTfcAjsNgh2NMHL1jRAarhzrKwqMCJFbQShalAEwokCvdnFidFsEsYoHsoZp6lLT2WVSkgdv5JT7a9eO8RELAMOIOJTytOrR8ei9LQpTizKAVVANycK9e4dOgSkn8uhwajs64NJD7Q1yJhuk+4E8O/NnXQhBXuh/t6eNmuxvUX1fQ4uavRZlAPcAW3w8Fo/oid75/Y8HuHPfzigZkEM7+k5ct7DH8D89lEL5DxdTWkLA767QXQFTh79eT++pATvGe+gHxcfJg3mI5dwyzx0EI0PAH4QN4G6gW4YSbkfZkXspeSAlrsrxjfgsZ/iIfKp8kug34M5HjPy5UmCibNlpUZBatMcrLwjgMyYBkipUpi9d8jgF5BrqMNZt9R1B9KMxk2jC2CXmJeO3ByZff/uqZ1ZBGbRuUSN5O4oapURbAqoWVUFwNxWDMalu+Q3jhSc+CeVPaKOXvbziI+28uddRsOt1Qb2qwjIdDsNlFvXVqva9PvLDW2Z9+9A7+xYMvNLxTXKihTlhkQIf3YNIQJWAY44W7YRDO6JkHzeXQBGpZ1PMcHfnzAoctCiNWrTxKD1uetzU3+fU6Ub0g/O+RjzVjdiRlB4rAelsxWVSxpWbFG9KcTypa4+Vd3T9xYF76cioi0kfUEVLnZfTskov9YQd5/Fb2JoQpr5wgO4VjCiLaJ1hC6597nTHfI3vi94iwKfSHXHebgJ3xL60LAPgBSnEoE+DVZZZYGrW9FUICa1/XwT6XWU4qzZJOq9M0O4kQQG3TnMRjEyXpjyvryX2PU8dPkKK56VE4wfMpvLud9LW9FTSQaUEJAsxXY4iiMHHKcjc4oQTk0dTKNwzVK8IH6pzKoB8JscR+UezGgxwTXJBuJWY/7coAxha5oABd6jcoen9i4v0Pit/jekNYBc4SVZlktCOIr4bDzz34+bXiomOOXXxnFeXSSNJDkc3OeKgzHWz6K4ef6D9w2bR8gun0I6UtjDqyPv8ZhMVxyXR8vZdUVlQIroHJyaxT2+y/8V8souSBPsA24aOI+cxJmFWtHhpcQYwBhoIUQbpG7v97yTKN9PeyW9QlSuRXK5y6lk+g+R7Q0jcgGyPkQfDOmZIKoldCMh8HocUCdDxgsEkKnyUPf92yn6PaOl/bqB1HfvS+DcGIVxKNGziS7RqwJW0vm0WVeqCEMeBegIFwQSm57lcojtlzOFkETLwVw4PtHdZJXXb+B5Jbz9yVxaAOAa5HJgr4WTDQ+DQQKY09jERTJ0KiCblIj/xMMQcYdNcTA4gJmxlwmsCT/IS29LQwzsQr0SVzOHUYcH9dMPz2fTAwhfoiv07yG2amIBg/RHdBWKOXkwabFEGMISso0tA06ySY+Q89Al0dYgy9iyj/m9dRV1euJfkFxeRuBoWrwuYoTo9KvhRoQoeTyrOJUNbHA1IzfJJbfca6+CU18xVGR1p4PoPcJ2kv01HGeh9LVFiX7P94gdClz1/vnzonbvNvkXHzTJdINaB6fpwqKOh7VlRNQ1xC5i2XGHpD4LFcUB94O4VKrYb8rYn59GFFOo9kQKJ7cj7l1VEb0JnJMA98oHoOm5mCwolTCcwJHgPIt8R2Ye+Mgs5btrK0bfSJcdyZfLy3wjZY4zQrSAZpbkIeGO2tvsURO8XyDgroPsSM8gNI+1EMNr2mupiyuoJnYWQ+simig/PemlRBrjgNBY6DLrk292U/clPyew4mo71nERxmRfQ4a6jKJ5OyOyyt4Xs2Z5APhiLCOFNcM0FyqQgHjD7oJRzexaKra7UYNble59J8fxs7fvPvE5tRVcKVZY5Cg9j9iol3tSTF4qAv0RLGHDT4c4XvXJPR/GX57+rSst3e+ok1+E5aBp2SccMcCls9mSkOj4YmbRpca+nLodblAEVmCSOB7LDNiLKcITo4OU3QbiROIoRjhcpapnHVggqgHi/m0g0EXTqdpJoU0eizxKIri6R1CePqFeJEP/21QNIsf3GcT4d2TZOuGlISjcZP+I7ESzcIo2kVBEoHqOv3XAHRDgP/WZOpxUzl9PLM+mButjWczxrAaayHfRgEJP76AM/LAbkukjceKKUMpY+iuntTEooPEhBVwKVtO1JGd99Tf4+7aUnCFflTQQhv8gkuhwKfzv2/wo6fAhf9Y5ErA7YJYrS7seil//7JW2TScGJl79n+A+QKF5dTAkDTBEs72fFZ23Wl+2Rvz7p6yzGuTdO/4o69ij3u8vdLhP2/xTVAtXIQWbD9FChqKI+am66lRymFu0BLHAjtv4NPiFsQcoQStn3NwrGdaSK9N7kqdhDxqIlQubDCJ8HX5K9oTfw1x0yPJ51hCbk63FCTJb0tTHt2Rs+fvE30/+wSzfyFjgDHa+mqg4/Oj/++JYB+p7NYvVDb3X7aYC2+50OPWeN6cDsZzAA4sMECBXnryP5UHTK4xKc0o50Qz9nT7SS0xRzBmDlNCgYLjM3LQ+mfvk7qujzE/LB4KZunk/C24GS1z9HJy58knJn/jsNKZpCctFAEt8g8DIYwgqvk5CRRXmwAz+Hce5Olu5L97qhlYYt+RjT/GRKwyVC3hSnXrSMApc/TmsHDE/oAiY7XXp1XiKPw2ogiQAUtWEg+R5OdUTWBvOkQY8QawuqcYpqIia7sWUAwHRbZnWbwz+8cSK5UskTDArviQ1U2fXHVJ7UmTK2L6CvhlxDVlsQuCfw6I8R8HxUfhdeTya20EByLwj0DOyAh7RQRcHfr9qiO9I3PzqWsobqrpK95Dr4QQg23tjUa9z8jSadvKAk6Cg1HFaDVKxDLg4UIs3Ehx7SC6srx3AKC26pVZ0dMadliWv373a8Aj7VnmCq02ATD6uJ1cT6taqxL1dkhO2YHDVoKDk846j8AIXisrUj/W8mV6CS2n/9Dpn9RtD6Tj1pjH8zr3kUEpQQx1DvW+DfB8Z4vU7ivipJ2d+hxWS6JHnYS/RLuo4uHTxWlpprYckzRcqobFPo64c/NPZKBgIRpiYV5JdehbygMVXl8I0i0VvQHV6tlAEM5o66tWDOz/tilMiOmQUgwzziewBcs0stjje5NY6/ALIKCPQ9az7g5NcxIUfCDVSxlSrbXWyanlRKObGHPGUYun65nQ72/wltjcdcpITaYX6xoz4Cg65boA8WQibycDweoQnYY/IXheSmPj+1dAwA/AU+UbQBq4x0N5VvJ33ZiplNhjlSEWoovHAscsyEdaBbFBtC9D5xkp6c/1KHkltnTeLLm+K0MlZbHNuK1Si7+T0AADlhy447hLhuz2bZ6/UbE+jikSuMsu1U2ek6JIuWa67jK4XbmUaWE17O4I60+oLJYemRyJcCxhhNYaR2HHFqhCDGtSNgj8FXfjitVhia2GpdLcx9oiplnF4w8FfDOs77CeWPf6L4ySd+9cCjsNfcRIR+Z75hr0eSH6qoGyeJYV91XZ7GVv4oesPYdXOtDm/+IpVGTvwbnvH2kp1fFy7ueyEHFjV2r2Hk+dnNKs1ngAIi7MN1OLYnDAwSQSl0MhTwpkh/ajeH15NCDsR/3Ls+pO23fkwb0jpSO3geUCGKdBKWW6XOMiOGHgqHJIK4xrOYB6EHPk4RNLEjBRwpFJe3A6vISFs39o4kVzrdkIRO09BI97SUYa6heQzAyFemaM4xQ7W85gSM+ei8g9RpzkyNeiFjwlcsxOj+MybMnUF9+8+wNl4wWdvQrpcVdOhY8se1Gs9/G7aYMACaRyRhOD935DQxqm23g+N/P3g/ZQ6/OGn382QmjTcDcZm6q+wgIT2WtmYPonLQ1q1EB4CrLcBhZcgm1IcfWz91wSPpHw4RkgmByvRuvvjDW+L0/Sv1gvG/Cq5Mbp+vHyMLQ1lpwoY0uoTTTSDHFAdGpOKJEo8VPKfQBggdSUij1f/9D+r/1fta/P6PLHJkoKu3Ee3WPCAu/90D1Oeht+jVsTOU9+ysWVHVaDAY7eYVBbnqAZYJN/7d3oP2i100lvLWjivt9Yuv9ECh7tn0Z0uYfrJSh9M1Hz1K1323jY5DvyP5wSZ/GAYmvBbWKXA/QAV08gVdyOh05FjC3uWH9FCZST3GkOEvv//Zy6lzv/yjw00SgyG/jf5Blw91tqFeeOKjWGbF/GZmqBC2A1JdgJPPDRhF60fcZtLRPE0acQVBZ9Ims/N0QW1JliRkCh8EqVbmTRiLRv3HpAfwE5mSDqDQ2Rd2Q8VuWn7lb1/84Jay4JARS35ndljzC02DuvCsXEtdBl1FxZ0uqLF+tQQYB3zMDX4J1bMNe6MFnFGZSbrupAMrv0xauvJVevcFugmBbNzVrPLYApnLKybxlHCfwC7bAANM6FMpZL+tGJa3YV9NlBiVxzqKvAVUOfzHtKjPxeRhxxVwBgRbg6aVmDGAaQZVDmAgFpEyuNyK+yrVQSum3C9GD7qaRm78hBL6fkcr+oyjNBZ0roQSNXYLqyEXdEAeDPbbEM1JaNVhHqL8+GIK5vb4Ztr7++iKGyb95ngg/YThDGJeONIK2oE4wkFRx7zPbdvH6jmR6+o+dEC8yKYM0dAJvBAcsChFxpWciE2wHbjk8D6ZtjpHyIyhlYg34UUJSRkYCMqvR/xE7ItzW50DJPzR6135IY0sMWNAfc+t0DWLMxrcmMNd1r4LfZV+PyVhkVeV04l5XQ5KhpW3WqHNZGOOKP2P/S9TCQtKkbYCLX8wbaD4/CuqnHpb8L2hU6f/nwqaXu5xkrea9PU9/QzOoT7HgWCI+clKcrhJnknjHjFy+yKsEKMSS3fs0qX7QipaTRWD7qAlPYYT8IK9wG2KzWfwrAZuab4NaKBhPq1jFMnzAQxoO59JWHhNJVg66eI8lfrqMRmcoMgxSP9qGJT+0Fl73UIe8igjvWv8Q47iFF21oxwotA1pbvKPhZdpbRempQMcKcRK5aEFxyhzwxzMuF2YqPuLi0wjbhvlE20cdTsd9Dgwx4GMGdS15cVuo7HbFu0BQtPYs0BB0gPWsyINRXk/VsRriby3oYYGNjabwYBFEMjOoNA2V0gTFYY1emj+5oSMh4Pl5PMjedHSeK6taYWZD9rBv6UyNHIJVtDcE/ATz0Lo6qVDuDBs53Ih8raQ2fliiVjIZL1opVE56n5a2nUIpaP3BsF53BZWo02GBJ5201BoRC2GMlK4azOstpXg1x3wpWrNg3WplI+o2xeQ+FuAJfKEqGfIot1gXv665342PeMPkaZitsHqxzIsIrkHaghxBksvwdijX3ERddn6ERLHJpGj9Fsy47tqeh7JXTdcsfm4Sx/YFpnEQUSSGI9m0F7h0PIMOA2p7B6g0LA5sxHzAEchixcgTLHCa1KWzwlTcXL39R99TMuuiX9laX5CristhBd5cMJEkwrWjUkIsIFJzkJMUWfxOjHIBsZUAhE3oot3Lif3gnlUPvUuhJ2CQYP8TiymWr+01/C30w0aaAYE+xt2f20SDHalVmWA3QOU+4RwMhWBrO9jDrgHKNIOS2lGYfHA4m9oy9QFcb8dcdniJ9aYxklPiuYU0vTjdTYYSkT1LxulM9jyAnDUReyVtVEcYoTsz+ulmD8eVIiV3EsepYprb6fy5C4y6GpDybmfc/rL83PSvYGfwUSVht/GaHfkM3hgw7e0KgMUWExCJgczYFM60QZIfxfQ5WsviVIYCwyx8kZ09ybEURcPUhtP4FIGKI+7yMfkQ2kaF1APFeGU8dsWLQOxH56GmLDxU8ATokPdx1DnPctD3sMfOs2E4YcN2j3352V0q8WDthiW1mcAE9GFvyPA7HXI5BjsV8BWtAFX8DZQgUyIpGDuri3FPb9qG/Rn9KiqbLcqNXkzMi0c7X2hFFPTzYAmQk3QBzx8R1IjZbk0GlKgCWtI/jEt6693UumF91G7g5uk98hijVxdECw0Xmaad6ryx/s51SKGpVUZUG0DuAdwRsQAGOF30BU6Y1DKqSnJsANb+pvD1k/rsSSv+CkkYvmoL827s4AW0TGaDYEtaS4t8Eqza10e+os/SObFWxZqhBQlEzM0cQV7GCq4Ws6jW8bOfIsWL6SAzmP92Bbuya1bAIEaLKQhLDlzL0a/CElfBb2wAxdWIUDpQWjV28YA8S158YjHZNooQV3730vDex+WgxJelYPTejUHgRSfP1gIlTb05HHKXvMcVfW/kdwlh6GTvHhRSCJtn/CrgoGPXKoYXWlobHxjWlq1BygjDDlDnjqplxtgOp0GHcYADDhemkJyRRamJTdR6dQHTy55/a6brv2fHv2k94KCQFIPp6U527gTJtwdOrbjTv+khI8XXvtswWfZPR0QWauDz8IUA97KZEBi0ZFUrCeabDiHDGo2AT4kxvepAu9Hb/+7Lk7uIiM+mypTulGbQytEKGsSfdrvUoPWRkbJUWGP6Oaas9+qDKgGXA0EcKTC0SALi1knzBPfnK/RRWTtrrwmfUlW9yvaz1hLw5a+lOo8/neq6HQNFVpaZRvN6XEK/3VXL32BLup/La06fzytT+NBn4bcLRhZZnD1g2rvsN0/iWF6v0P5VvfVL2glg2aSpziXXOXHSVuzldZN/yPt9zrl/sVvyezaVWN2dE4wgEVRFbakEWsq/XBHBfzDPqQVnnT5Avnke7/3MMfadn1p4vYlVq+1r7ri/Hlezi0s7TCuGERztn9vkuuajHGhwQNuoZUXXibXpGUeTwtiRayUBgaBp6gPTCtiFCbco3ct84q9m8mDlBlLd5H7wFyMAe6mZdn9KBWdcnmXsREAY79pVQbYRpgDWtVMiOAo9AqEw+CkwD28yPjzY5euG/TGh4O/bTu1fc8A9b5G7pSW0Wv1S9dLp+vehKLdmZZpnvR1vy1Xrzjh7vrh7ZldNwzxTOl59ScZt896+toK4R5STo4SDHUNBNFCePsEVmQas/q4Cv76xZapvXd+/nvq1DbkLP3OqMgcCi8MHvHwGZSHcFQqRmu5WVmxp3ykxVZlQPVArD70VJgRXpCRQinxL+6hX79YNDX8ykh193nhOrOxmQ3+/VRLpmluB/XjyRKz+6V+ESj1p2944nq56tG2mCC66aP6noFzk3tSD0oyyGwzEhoe3s93b1DZkLtpZdZASoDPw5MEdgdtoIlmnW5VBtg9oH4MeKDpQGpzIR2qeGl0yrz78p+Ny08tRDIjA81X3aGAfsLl9Yts18Y/bjtROHTLkp932LdignfNHB8mUfIpyZVCSdk3LvqPT/42P7tfbt+iovhcPTnUNlApDid4Q2P3f5Mo351xt5WAhR3eNENDgmjcFsT/pt9MB7xOyob64XebwUlqsdKqDLB7QF31E8YWYWhQEYvL5Bb/5AcvQgbzdqxtUlmEEZG0jDi1VOmB/USLXRn0/0bcXN6//81y8sB/d/ffuKhjh/fepT133W++0a7fW2kwJ1vcyZQIy3vQ60WmL9GM8pNYe7COjvSbLkuS2mld9v6DggMvpxXdhlASiG/hHvSuFi2tygAbs/psAKZJ0PVBNSiGvs7F9Fh8HrmQSRt+Ka9NFqgHDrDih1QufoFjfFWcTv52bpk3qLcwb7pZBtyGfr9rGRl4A5D0Cer8p02UP6AXlWUl0XmLn7Z8Xa8XvrhEkZG3kxK2vkxf3/g5YbYLo16TQgh9tKT6YfxblQGnV0EIR6t3PCVRtvdemc0vLzt9gXePEs5uw9tAsIelZqoWjwYQ4abV+L2N7Met2HI4NYgb0npQx51LyFO0jayUAbS6x0il+/GSaRVvwl0tWlqVAdWY1StmfJJ//Bq3DggVe0ESpnH45lN6DbMHl9R5vs/i1cPoQRKxZ92jQgxiPoJ9zJEM8KUSYwW9jLTS49LXZRx5c/9IuePn0I7EJEoLWsg10qrnLVCpxUqrMsC2AUpKw3StB1GeeToSIXvN5VPsRqR+fefV0IKDLlPLIPkpRH/GO3M6ghFdwQ4rWSTlLie8jo8O3dZXRcYbBKXm8THba1UGnIoFo16fpmGfx77WGPLYbUXqXngcK24Kia7BpM9qzLz9Gf5Ne7yNVV9LVZfeQuu6XKiWuCLX6BSGnwprbM60KgNsUobnNxghDjbaRIsNgrVaQYyPHLAtvfOw7gC/MRkUXDSMnL8nWnvb/bQn3kuZgbD6aUkwomFqVQaw+8JFQk8LwVbTxUfqXIv88eMQkBA+vH9YZJLW61vanvwz2jTgdTqemahcT5b+s1lahQE2iuoLCjjQMFTlIpo8y9sIkvHDYQ80iXR4REGDWPP3SYd0GowMLl5Wy1kRZ7O0KAOis9KikWIZ58yUoIQOBtI+62rQRA2xom9rwX1OQfeQW/uETMugbKgmgTlJOyjbgg8+pekWZQBE2uKMAxT+57WJqoMHMWxKBQNWVQyiTVVYKXeWu72CCH88SKjCcJffFRLgoZx9gbca1qJZ+A/7vuoKPNOY68eWZQA7GOzAYMAH4GuBz8j6MQ6swMeNeD/mmPFTT1PsZ3J2Fv/qFownNPUubCzEt69F5Mc+jMm2uvGYtMaNREkM3tIgIz0gBGlCZCU8TgK+it7smmN1jUIfplGdCysCbubsHPPXTniiy4aJUWBC49jEpBoUE1yzA3xWTRDXyacPn2/Of+wYoCIyAIXXNUYKpgLfxKc+FoDQpgopsq61L6pb+SB8RlFfXTv7x9Ew1UCElTfIHsZwuJKywr0AooLFSQrI6L8a0KPPnuF+kxhwPRqfF/UAIMDkV1+vAzSD+NsyT10hVuF3BLfx7wdRoJZuUansEalhXjD17e8HbGwClk1iQDXxa1tPXi6F9+/h4wpB+hifnp0FT2cbGGNADTVLSpqAV0yqQNpZN1lYQ+wFLg8ipXEkFvRxsjdbNlZH3HlY6ap+MagJT20SA/qmk4gwgT+IFl2Y+JxWk4ZPjMyJfNMFljj6lu/ZPqjsQs4Yqx68yI/NhcKGiR/pARg7c9QPqetN6AKKc40mif0VJUGrI8Tl6K1dFBPwTv5wBibL/vf5B7x4AQfSF/kLGmF64RxchBAEjeVvzzNXiIOM/Gt3AudGlib1gMevp2AOHuSqojewnv0/ISHtIe2VoLOLYcMlBtSCSmo0QKh3bhUWHuAEAVNqJ6JMgzDIBlQS55Y+xzfwt+xxE09iNKo0iQE8kZKDj7nhDbPl/C0YvG3ki7hE8mK5v/2uTWZDg4qHORTGKwxr3eO6GERfj96ve18sjutrn8+pAqB5UQBe8OTiD1Pge8XP48vd7/JHifAt+0YTn9usbjv8hMb9R5gQwiKHHjC0s9HaBEhKUvNabRwMZ/tujG1Y+ndCBT2Hb9f/ST0/vMgtWqbOGKxmMYCfYjOB9/9jgcx0W9QWE0o8imwSQNzOuVp0vEYZCXfl+KTVHoaRJT+8Va+JbT2wmQnVs1utB8ZZfXKscG52D6jBWoqcHCifx2vO/CD3niB6/HGOWodDJT9IHP+F1L8ocNYo8L9ln+xFqvL90AAAAABJRU5ErkJggg=="}}]);