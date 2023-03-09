"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[474],{3905:(e,n,t)=>{t.d(n,{Zo:()=>p,kt:()=>f});var a=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function l(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function i(e,n){if(null==e)return{};var t,a,r=function(e,n){if(null==e)return{};var t,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var g=a.createContext({}),s=function(e){var n=a.useContext(g),t=n;return e&&(t="function"==typeof e?e(n):l(l({},n),e)),t},p=function(e){var n=s(e.components);return a.createElement(g.Provider,{value:n},e.children)},c="mdxType",m={inlineCode:"code",wrapper:function(e){var n=e.children;return a.createElement(a.Fragment,{},n)}},u=a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,o=e.originalType,g=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),c=s(t),u=r,f=c["".concat(g,".").concat(u)]||c[u]||m[u]||o;return t?a.createElement(f,l(l({ref:n},p),{},{components:t})):a.createElement(f,l({ref:n},p))}));function f(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var o=t.length,l=new Array(o);l[0]=u;var i={};for(var g in n)hasOwnProperty.call(n,g)&&(i[g]=n[g]);i.originalType=e,i[c]="string"==typeof e?e:r,l[1]=i;for(var s=2;s<o;s++)l[s]=t[s];return a.createElement.apply(null,l)}return a.createElement.apply(null,t)}u.displayName="MDXCreateElement"},5162:(e,n,t)=>{t.d(n,{Z:()=>l});var a=t(7294),r=t(6010);const o="tabItem_Ymn6";function l(e){let{children:n,hidden:t,className:l}=e;return a.createElement("div",{role:"tabpanel",className:(0,r.Z)(o,l),hidden:t},n)}},4866:(e,n,t)=>{t.d(n,{Z:()=>_});var a=t(7462),r=t(7294),o=t(6010),l=t(2466),i=t(6550),g=t(1980),s=t(7392),p=t(12);function c(e){return function(e){return r.Children.map(e,(e=>{if((0,r.isValidElement)(e)&&"value"in e.props)return e;throw new Error(`Docusaurus error: Bad <Tabs> child <${"string"==typeof e.type?e.type:e.type.name}>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.`)}))}(e).map((e=>{let{props:{value:n,label:t,attributes:a,default:r}}=e;return{value:n,label:t,attributes:a,default:r}}))}function m(e){const{values:n,children:t}=e;return(0,r.useMemo)((()=>{const e=n??c(t);return function(e){const n=(0,s.l)(e,((e,n)=>e.value===n.value));if(n.length>0)throw new Error(`Docusaurus error: Duplicate values "${n.map((e=>e.value)).join(", ")}" found in <Tabs>. Every value needs to be unique.`)}(e),e}),[n,t])}function u(e){let{value:n,tabValues:t}=e;return t.some((e=>e.value===n))}function f(e){let{queryString:n=!1,groupId:t}=e;const a=(0,i.k6)(),o=function(e){let{queryString:n=!1,groupId:t}=e;if("string"==typeof n)return n;if(!1===n)return null;if(!0===n&&!t)throw new Error('Docusaurus error: The <Tabs> component groupId prop is required if queryString=true, because this value is used as the search param name. You can also provide an explicit value such as queryString="my-search-param".');return t??null}({queryString:n,groupId:t});return[(0,g._X)(o),(0,r.useCallback)((e=>{if(!o)return;const n=new URLSearchParams(a.location.search);n.set(o,e),a.replace({...a.location,search:n.toString()})}),[o,a])]}function d(e){const{defaultValue:n,queryString:t=!1,groupId:a}=e,o=m(e),[l,i]=(0,r.useState)((()=>function(e){let{defaultValue:n,tabValues:t}=e;if(0===t.length)throw new Error("Docusaurus error: the <Tabs> component requires at least one <TabItem> children component");if(n){if(!u({value:n,tabValues:t}))throw new Error(`Docusaurus error: The <Tabs> has a defaultValue "${n}" but none of its children has the corresponding value. Available values are: ${t.map((e=>e.value)).join(", ")}. If you intend to show no default tab, use defaultValue={null} instead.`);return n}const a=t.find((e=>e.default))??t[0];if(!a)throw new Error("Unexpected error: 0 tabValues");return a.value}({defaultValue:n,tabValues:o}))),[g,s]=f({queryString:t,groupId:a}),[c,d]=function(e){let{groupId:n}=e;const t=function(e){return e?`docusaurus.tab.${e}`:null}(n),[a,o]=(0,p.Nk)(t);return[a,(0,r.useCallback)((e=>{t&&o.set(e)}),[t,o])]}({groupId:a}),y=(()=>{const e=g??c;return u({value:e,tabValues:o})?e:null})();(0,r.useEffect)((()=>{y&&i(y)}),[y]);return{selectedValue:l,selectValue:(0,r.useCallback)((e=>{if(!u({value:e,tabValues:o}))throw new Error(`Can't select invalid tab value=${e}`);i(e),s(e),d(e)}),[s,d,o]),tabValues:o}}var y=t(2389);const F="tabList__CuJ",h="tabItem_LNqP";function v(e){let{className:n,block:t,selectedValue:i,selectValue:g,tabValues:s}=e;const p=[],{blockElementScrollPositionUntilNextRender:c}=(0,l.o5)(),m=e=>{const n=e.currentTarget,t=p.indexOf(n),a=s[t].value;a!==i&&(c(n),g(a))},u=e=>{let n=null;switch(e.key){case"Enter":m(e);break;case"ArrowRight":{const t=p.indexOf(e.currentTarget)+1;n=p[t]??p[0];break}case"ArrowLeft":{const t=p.indexOf(e.currentTarget)-1;n=p[t]??p[p.length-1];break}}n?.focus()};return r.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,o.Z)("tabs",{"tabs--block":t},n)},s.map((e=>{let{value:n,label:t,attributes:l}=e;return r.createElement("li",(0,a.Z)({role:"tab",tabIndex:i===n?0:-1,"aria-selected":i===n,key:n,ref:e=>p.push(e),onKeyDown:u,onClick:m},l,{className:(0,o.Z)("tabs__item",h,l?.className,{"tabs__item--active":i===n})}),t??n)})))}function k(e){let{lazy:n,children:t,selectedValue:a}=e;if(n){const e=t.find((e=>e.props.value===a));return e?(0,r.cloneElement)(e,{className:"margin-top--md"}):null}return r.createElement("div",{className:"margin-top--md"},t.map(((e,n)=>(0,r.cloneElement)(e,{key:n,hidden:e.props.value!==a}))))}function S(e){const n=d(e);return r.createElement("div",{className:(0,o.Z)("tabs-container",F)},r.createElement(v,(0,a.Z)({},e,n)),r.createElement(k,(0,a.Z)({},e,n)))}function _(e){const n=(0,y.Z)();return r.createElement(S,(0,a.Z)({key:String(n)},e))}},8107:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>p,contentTitle:()=>g,default:()=>u,frontMatter:()=>i,metadata:()=>s,toc:()=>c});var a=t(7462),r=(t(7294),t(3905)),o=t(4866),l=t(5162);const i={sidebar_position:3,id:"log",title:"Log - Cats"},g=void 0,s={unversionedId:"cats/log",id:"cats/log",title:"Log - Cats",description:"Log - Cats (WIP)",source:"@site/../generated-docs/docs/cats/log.md",sourceDirName:"cats",slug:"/cats/log",permalink:"/docs/cats/log",draft:!1,tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3,id:"log",title:"Log - Cats"},sidebar:"theSidebar",previous:{title:"What to Import",permalink:"/docs/cats/import"}},p={},c=[{value:"Log - Cats (WIP)",id:"log---cats-wip",level:2},{value:"Log <code>String</code>",id:"log-string",level:2},{value:"Example",id:"example",level:3},{value:"Log <code>F[A]</code>",id:"log-fa",level:2},{value:"Example",id:"example-1",level:3},{value:"Log <code>F[Option[A]]</code>",id:"log-foptiona",level:2},{value:"Example",id:"example-2",level:3},{value:"Log <code>F[Either[A, B]]</code>",id:"log-feithera-b",level:2},{value:"Example",id:"example-3",level:3},{value:"Log <code>OptionT[F, A]</code>",id:"log-optiontf-a",level:2},{value:"Example",id:"example-4",level:3},{value:"Log <code>EitherT[F, A, B]</code>",id:"log-eithertf-a-b",level:2}],m={toc:c};function u(e){let{components:n,...t}=e;return(0,r.kt)("wrapper",(0,a.Z)({},m,t,{components:n,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"log---cats-wip"},"Log - Cats (WIP)"),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"Log")," is an algebra to log ",(0,r.kt)("inlineCode",{parentName:"p"},"F[A]"),", ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Option[A]]"),", ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),", ",(0,r.kt)("inlineCode",{parentName:"p"},"OptionT[F, A]")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"EitherT[F, A, B]"),". So ",(0,r.kt)("inlineCode",{parentName:"p"},"Log")," provides abstraction of logging operations, and there can multiple interpretations if required.  "),(0,r.kt)("p",null,"More precisely, it requires ",(0,r.kt)("inlineCode",{parentName:"p"},"Fx")," from ",(0,r.kt)("a",{parentName:"p",href:"https://kevin-lee.github.io/effectie"},"Effectie")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"Monad")," from ",(0,r.kt)("a",{parentName:"p",href:"https://typelevel.org/cats"},"Cats"),". So it can be used for whatever effect you want as long as there's an interpreter for ",(0,r.kt)("inlineCode",{parentName:"p"},"Fx")," of the effect."),(0,r.kt)("h2",{id:"log-string"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"String")),(0,r.kt)("p",null,"LoggerF is mainly for ",(0,r.kt)("inlineCode",{parentName:"p"},"F[_]")," but let's start with more simple logging case that is logging ",(0,r.kt)("inlineCode",{parentName:"p"},"String"),"."),(0,r.kt)(o.Z,{groupId:"logS",defaultValue:"syntax",values:[{label:"With syntax",value:"syntax"},{label:"Without syntax",value:"no-syntax"}],mdxType:"Tabs"},(0,r.kt)(l.Z,{value:"syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"logS(String)(debug) // F[String]\nlogS(String)(info)  // F[String]\nlogS(String)(warn)  // F[String]\nlogS(String)(error) // F[String]\n// or\nString.logS(debug) // F[String]\nString.logS(info)  // F[String]\nString.logS(warn)  // F[String]\nString.logS(error) // F[String]\n")),(0,r.kt)("p",null,"If you don't need to re-use the ",(0,r.kt)("inlineCode",{parentName:"p"},"String")," value,"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"import loggerf.syntax.all._\n\nlogS_(String)(debug) // F[Unit]\nlogS_(String)(info)  // F[Unit]\nlogS_(String)(warn)  // F[Unit]\nlogS_(String)(error) // F[Unit]\n// or\nString.logS_(debug) // F[Unit]\nString.logS_(info)  // F[Unit]\nString.logS_(warn)  // F[Unit]\nString.logS_(error) // F[Unit]\n"))),(0,r.kt)(l.Z,{value:"no-syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"import loggerf.core._\n\nLog[F].logS(String)(debug) // F[String]\nLog[F].logS(String)(info)  // F[String]\nLog[F].logS(String)(warn)  // F[String]\nLog[F].logS(String)(error) // F[String]\n")),(0,r.kt)("p",null,"If you don't need to re-use the ",(0,r.kt)("inlineCode",{parentName:"p"},"String")," value,"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].logS_(String)(debug) // F[Unit]\nLog[F].logS_(String)(info)  // F[Unit]\nLog[F].logS_(String)(warn)  // F[Unit]\nLog[F].logS_(String)(error) // F[Unit]\n")))),(0,r.kt)("h3",{id:"example"},"Example"),(0,r.kt)(o.Z,{groupId:"logS-example",defaultValue:"syntax",values:[{label:"With syntax",value:"syntax"},{label:"Without syntax",value:"no-syntax"}],mdxType:"Tabs"},(0,r.kt)(l.Z,{value:"syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {\n  greeting <- logS(s"[1] Hello $name")(info)  // F[String]\n  _        <- effectOf(println(greeting)) // F[Unit]\n  // Or\n  greeting2 <- s"[2] Hello $name".logS(info)  // F[String]\n  _         <- effectOf(println(greeting2)) // F[Unit]\n} yield ()\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\nimport loggerf.logger._\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@4d789967\n\nhello[IO]("Kevin").unsafeRunSync()\n// [1] Hello Kevin\n// [2] Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"22:52:36.970 [Thread-64] INFO test-logger - [1] Hello Kevin\n22:52:36.977 [Thread-64] INFO test-logger - [2] Hello Kevin\n")),(0,r.kt)("hr",null),(0,r.kt)("p",null,"If you don't need to re-use the ",(0,r.kt)("inlineCode",{parentName:"p"},"String")," value,"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {\n  _ <- logS_(s"[1] The name is $name")(info) // F[Unit]\n  // Or\n  _ <- s"[2] The name is $name".logS_(info)  // F[Unit]\n  \n  _ <- effectOf(println(s"Hello $name")) // F[Unit]\n} yield ()\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\nimport loggerf.logger._\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@29c19641\n\nhello[IO]("Kevin").unsafeRunSync()\n// Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"22:53:09.165 [Thread-66] INFO test-logger - [1] The name is Kevin\n22:53:09.166 [Thread-66] INFO test-logger - [2] The name is Kevin\n"))),(0,r.kt)(l.Z,{value:"no-syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {\n  greeting <- Log[F].logS(s"Hello $name")(info) // F[String]\n  _        <- effectOf(println(greeting))       // F[Unit]\n} yield ()\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\nimport loggerf.logger._\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@369191a7\n\nhello[IO]("Kevin").unsafeRunSync()\n// Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"02:07:09.298 [Thread-67] INFO test-logger - Hello Kevin\n")),(0,r.kt)("hr",null),(0,r.kt)("p",null,"If you don't need to re-use the ",(0,r.kt)("inlineCode",{parentName:"p"},"String")," value,"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef hello[F[_]: Fx: Log: Monad](name: String): F[Unit] = for {\n  _ <- logS_(s"The name is $name")(info) // F[Unit]\n  _ <- effectOf(println(s"Hello $name")) // F[Unit]\n} yield ()\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\nimport loggerf.logger._\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("test-logger")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@2f0955da\n\nhello[IO]("Kevin").unsafeRunSync()\n// Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"02:15:41.367 [Thread-74] INFO test-logger - The name is Kevin\n")))),(0,r.kt)("h2",{id:"log-fa"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"F[A]")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].log(F[A])(A => LogMessage) // F[A]\n")),(0,r.kt)("p",null,"or with ",(0,r.kt)("inlineCode",{parentName:"p"},"loggerf.syntax")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"F[A].log(A => LogMessage) // F[A]\n")),(0,r.kt)("p",null,"A given ",(0,r.kt)("inlineCode",{parentName:"p"},"F[A]"),", you can simply log ",(0,r.kt)("inlineCode",{parentName:"p"},"A")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,r.kt)(o.Z,{groupId:"log-fa",defaultValue:"syntax",values:[{label:"With Syntax",value:"syntax"},{label:"Without Syntax",value:"no-syntax"}],mdxType:"Tabs"},(0,r.kt)(l.Z,{value:"syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import loggerf.syntax.all._\n\nval fa: F[A] = ...\nfa.log(a => debug(s"Some meesage: $a")) // F[A]\nfa.log(a => info(s"Some meesage: $a"))  // F[A]\nfa.log(a => warn(s"Some meesage: $a"))  // F[A]\nfa.log(a => error(s"Some meesage: $a")) // F[A]\n// OR\nlog(fa)(a => debug(s"Some meesage: $a")) // F[A]\nlog(fa)(a => info(s"Some meesage: $a"))  // F[A]\nlog(fa)(a => warn(s"Some meesage: $a"))  // F[A]\nlog(fa)(a => error(s"Some meesage: $a")) // F[A]\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import effectie.core._\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef count[F[_]: Fx: Log](): F[Count] =\n  counter.currentCount() // F[Count]\n    .log(count => info(s"Current count: $count")) // F[Count]\n'))),(0,r.kt)(l.Z,{value:"no-syntax",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'val fa: F[A] = ...\nLog[F].log(fa)(a => debug(s"Some meesage: $a")) // F[A]\nLog[F].log(fa)(a => info(s"Some meesage: $a"))  // F[A]\nLog[F].log(fa)(a => warn(s"Some meesage: $a"))  // F[A]\nLog[F].log(fa)(a => error(s"Some meesage: $a")) // F[A]\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import effectie.core._\nimport loggerf.core._\nimport loggerf.syntax.all._\n\ndef count[F[_]: Fx: Log](): F[Count] =\n  Log[F].log(counter.currentCount())(count => info(s"Current count: $count"))\n')))),(0,r.kt)("h3",{id:"example-1"},"Example"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\ndef hello[F[_]: Functor: Fx: Log](name: String): F[Unit] =\n  s"Hello $name".logS(debug).map(println(_))\n \nobject MyApp extends IOApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  import effectie.instances.ce2.fx._\n  import loggerf.instances.cats._\n  \n  def run(args: List[String]): IO[ExitCode] = for {\n    _ <- hello[IO]("World")\n    _ <- hello[IO]("Kevin")\n  } yield ExitCode.Success\n}\n\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"23:34:25.021 [ioapp-compute-1] DEBUG MyApp - Hello World\nHello World\n23:34:25.022 [ioapp-compute-1] DEBUG MyApp - Hello Kevin\nHello Kevin\n")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'trait Named[A] {\n  def name(a: A): String\n}\n\nobject Named {\n  def apply[A: Named]: Named[A] = implicitly[Named[A]]\n}\n\nfinal case class GivenName(givenName: String) extends AnyVal\nfinal case class Surname(surname: String) extends AnyVal\n\nfinal case class Person(givenName: GivenName, surname: Surname)\nobject Person {\n  implicit val namedPerson: Named[Person] =\n    person => s"${person.givenName.givenName} ${person.surname.surname}"\n}\n\nimport cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\ntrait Greeting[F[_]] {\n  def greet[A: Named](a: A): F[String]\n}\n\nobject Greeting {\n  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]\n\n  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =\n    new Greeting[F] {\n      def greet[A: Named](a: A): F[String] = for {\n        name     <- effectOf(Named[A].name(a)).log(x => info(s"The name is $x"))\n        greeting <- pureOf(s"Hello $name").log(greet => debug(s"Greeting: $greet"))\n      } yield greeting\n    }\n\n}\n\nobject MyApp extends IOApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  import effectie.instances.ce2.fx._\n  import loggerf.instances.cats._\n  \n  def run(args: List[String]): IO[ExitCode] = for {\n    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))\n    _ <- ConsoleFx[IO].putStrLn(greetingMessage)\n  } yield ExitCode.Success\n}\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"23:04:56.272 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee\n23:04:56.273 [ioapp-compute-0] DEBUG MyApp - Greeting: Hello Kevin Lee\nHello Kevin Lee\n")),(0,r.kt)("h2",{id:"log-foptiona"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"F[Option[A]]")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].log(\n  F[Option[A]]\n)(\n  ifEmpty: => LogMessage with MaybeIgnorable,\n  toLeveledMessage: A => LogMessage with MaybeIgnorable\n)\n")),(0,r.kt)("p",null,"A given ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Option[A]]"),", you can simply log ",(0,r.kt)("inlineCode",{parentName:"p"},"Some(A)")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"None")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,r.kt)("h3",{id:"example-2"},"Example"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\ndef greeting[F[_]: Fx](name: String): F[String] =\n  pureOf(s"Hello $name")\n\ndef hello[F[_]: Monad: Fx: Log](maybeName: Option[String]): F[Unit] =\n  for {\n    name    <- pureOf(maybeName).log(\n                 warn("No name given"),\n                 name => info(s"Name: $name")\n               )\n    message <- name.traverse(greeting[F]).log(ignore, msg => info(s"Message: $msg"))\n    _       <- effectOf(message.foreach(msg => println(msg)))\n  } yield ()\n\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp- F[Option[A]]")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@27a742ba\n\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\n\ndef run(): IO[Unit] = for {\n  _ <- hello[IO](none)\n  _ <- hello[IO]("Kevin".some)\n} yield ()\n\nrun().unsafeRunSync()\n// Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"20:09:43.117 [Thread-31] WARN MyApp- F[Option[A]] - No name given\n20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Name: Kevin\n20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Message: Hello Kevin\n")),(0,r.kt)("h2",{id:"log-feithera-b"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"F[Either[A, B]]")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].log(\n  F[Either[A, B]]\n)(\n  leftToMessage: A => LeveledMessage with MaybeIgnorable,\n  rightToMessage: B => LeveledMessage with MaybeIgnorable\n)\n")),(0,r.kt)("p",null,"A given ",(0,r.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),", you can simply log ",(0,r.kt)("inlineCode",{parentName:"p"},"Left(A)")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"Right(B)")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,r.kt)("h3",{id:"example-3"},"Example"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\ndef foo[F[_]: Fx](a: Int): F[Int] =\n  pureOf(a * 2)\n\ndef divide[F[_]: Fx: CanHandleError](a: Int, b: Int): F[Either[String, Int]] =\n  effectOf((a / b).asRight[String])\n    .handleNonFatal{ err =>\n      err.getMessage.asLeft[Int]\n    }\n\ndef calculate[F[_]: Monad: Fx: CanHandleError: Log](n: Int): F[Unit] =\n  for {\n    a      <- foo(n).log(\n                n => info(s"n: ${n.toString}")\n              )\n    result <- divide(1000, a).log(\n                err => error(s"Error: $err"),\n                r => info(s"Result: ${r.toString}")\n              )\n    _      <- effectOf(println(result.fold(err => s"Error: $err", r => s"1000 / ${a.toString} = ${r.toString}")))\n  } yield ()\n\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp - F[Either[A, B]]")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@2f1ab91\n\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\n\ndef run(): IO[Unit] = for {\n  _ <- calculate[IO](5)\n  _ <- calculate[IO](0)\n} yield ()\n\nrun().unsafeRunSync()\n// 1000 / 10 = 100\n// Error: / by zero\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"20:20:05.588 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 10\n20:20:05.593 [Thread-47] INFO MyApp - F[Either[A, B]] - Result: 100\n20:20:05.595 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 0\n20:20:05.605 [Thread-47] ERROR MyApp - F[Either[A, B]] - Error: / by zero\n")),(0,r.kt)("h2",{id:"log-optiontf-a"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"OptionT[F, A]")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].log(\n  OptionT[F, A]\n)(\n  ifEmpty: => LogMessage with MaybeIgnorable,\n  toLeveledMessage: A => LogMessage with MaybeIgnorable\n)\n")),(0,r.kt)("p",null,"A given ",(0,r.kt)("inlineCode",{parentName:"p"},"OptionT[F, A]"),", you can simply log it with ",(0,r.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,r.kt)("h3",{id:"example-4"},"Example"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.data._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.core._\nimport effectie.syntax.all._\n\nimport loggerf.core._\nimport loggerf.syntax.all._\nimport loggerf.logger._\n\ndef greeting[F[_]: Fx](name: String): F[String] =\n  pureOf(s"Hello $name")\n\ndef hello[F[_]: Monad: Fx: Log](maybeName: Option[String]): F[Unit] =\n  (for {\n    name    <- OptionT(pureOf(maybeName))\n                 .log(\n                   warn("No name given"),\n                   name => info(s"Name: $name")\n                 )\n    message <- OptionT.liftF(greeting[F](name))\n                 .log(ignore, msg => info(s"Message: $msg"))\n  } yield message)\n    .foreachF(msg => effectOf(println(msg)))\n\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp- OptionT[F, A]")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@38571ec1\n\nimport effectie.instances.ce2.fx._\nimport loggerf.instances.cats._\n\ndef run(): IO[Unit] = for {\n  _ <- hello[IO](none)\n  _ <- hello[IO]("Kevin".some)\n} yield ()\n\nrun().unsafeRunSync()\n// Hello Kevin\n')),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"21:49:30.118 [Thread-58] WARN MyApp- OptionT[F, A] - No name given\n21:49:30.119 [Thread-58] INFO MyApp- OptionT[F, A] - Name: Kevin\n21:49:30.121 [Thread-58] INFO MyApp- OptionT[F, A] - Message: Hello Kevin\n")),(0,r.kt)("h2",{id:"log-eithertf-a-b"},"Log ",(0,r.kt)("inlineCode",{parentName:"h2"},"EitherT[F, A, B]")))}u.isMDXComponent=!0}}]);