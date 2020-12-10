(window.webpackJsonp=window.webpackJsonp||[]).push([[11],{66:function(e,n,t){"use strict";t.r(n),t.d(n,"frontMatter",(function(){return i})),t.d(n,"metadata",(function(){return c})),t.d(n,"rightToc",(function(){return l})),t.d(n,"default",(function(){return s}));var r=t(2),a=t(6),o=(t(0),t(71)),i={id:"log",title:"Log - Cats"},c={unversionedId:"cats-effect/log",id:"cats-effect/log",isDocsHomePage:!1,title:"Log - Cats",description:"Log - Cats (WIP)",source:"@site/../generated-docs/target/mdoc/cats-effect/log.md",slug:"/cats-effect/log",permalink:"/docs/cats-effect/log",version:"current",sidebar:"someSidebar",previous:{title:"For Cats Effect",permalink:"/docs/cats-effect/cats-effect"},next:{title:"For Monix",permalink:"/docs/monix/monix"}},l=[{value:"Log - Cats (WIP)",id:"log---cats-wip",children:[]},{value:"Log <code>F[A]</code>",id:"log-fa",children:[{value:"Example",id:"example",children:[]}]},{value:"Log <code>F[Option[A]]</code>",id:"log-foptiona",children:[]},{value:"Log <code>OptionT[F, A]</code>",id:"log-optiontf-a",children:[]},{value:"Log <code>F[Either[A, B]]</code>",id:"log-feithera-b",children:[]},{value:"Log <code>EitherT[F, A, B]</code>",id:"log-eithertf-a-b",children:[]}],p={rightToc:l};function s(e){var n=e.components,t=Object(a.a)(e,["components"]);return Object(o.b)("wrapper",Object(r.a)({},p,t,{components:n,mdxType:"MDXLayout"}),Object(o.b)("h2",{id:"log---cats-wip"},"Log - Cats (WIP)"),Object(o.b)("p",null,Object(o.b)("inlineCode",{parentName:"p"},"Log")," is a typeclass to log ",Object(o.b)("inlineCode",{parentName:"p"},"F[A]"),", ",Object(o.b)("inlineCode",{parentName:"p"},"F[Option[A]]"),", ",Object(o.b)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),", ",Object(o.b)("inlineCode",{parentName:"p"},"OptionT[F, A]")," and ",Object(o.b)("inlineCode",{parentName:"p"},"EitherT[F, A, B]"),"."),Object(o.b)("p",null,"It requires ",Object(o.b)("inlineCode",{parentName:"p"},"EffectConstructor")," from ",Object(o.b)("a",Object(r.a)({parentName:"p"},{href:"https://kevin-lee.github.io/effectie"}),"Effectie")," and ",Object(o.b)("inlineCode",{parentName:"p"},"Monad")," from ",Object(o.b)("a",Object(r.a)({parentName:"p"},{href:"https://typelevel.org/cats"}),"Cats"),"."),Object(o.b)("h2",{id:"log-fa"},"Log ",Object(o.b)("inlineCode",{parentName:"h2"},"F[A]")),Object(o.b)("pre",null,Object(o.b)("code",Object(r.a)({parentName:"pre"},{className:"language-scala"}),"Log[F].log(F[A])(A => String)\n")),Object(o.b)("h3",{id:"example"},"Example"),Object(o.b)("pre",null,Object(o.b)("code",Object(r.a)({parentName:"pre"},{className:"language-scala"}),'trait Named[A] {\n  def name(a: A): String\n}\n\nobject Named {\n  def apply[A: Named]: Named[A] = implicitly[Named[A]]\n}\n\nfinal case class GivenName(givenName: String) extends AnyVal\nfinal case class Surname(surname: String) extends AnyVal\n\nfinal case class Person(givenName: GivenName, surname: Surname)\nobject Person {\n  implicit val namedPerson: Named[Person] =\n    person => s"${person.givenName.givenName} ${person.surname.surname}"\n}\n\nimport cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.cats.EffectConstructor\nimport effectie.cats.ConsoleEffect\nimport effectie.cats.Effectful._\n\nimport loggerf.cats._\nimport loggerf.logger._\nimport loggerf.syntax._\n\ntrait Greeting[F[_]] {\n  def greet[A: Named](a: A): F[String]\n}\n\nobject Greeting {\n  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]\n\n  implicit def hello[F[_]: EffectConstructor: Monad: Log]: Greeting[F] =\n    new Greeting[F] {\n      def greet[A: Named](a: A): F[String] = for {\n        name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))\n        greeting <- pureOf(s"Hello $name")\n      } yield greeting\n    }\n\n}\n\nobject MyApp extends IOApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  def run(args: List[String]): IO[ExitCode] = for {\n    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))\n    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)\n  } yield ExitCode.Success\n}\n')),Object(o.b)("pre",null,Object(o.b)("code",Object(r.a)({parentName:"pre"},{}),"21:02:15.323 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee\nHello Kevin Lee\n")),Object(o.b)("h2",{id:"log-foptiona"},"Log ",Object(o.b)("inlineCode",{parentName:"h2"},"F[Option[A]]")),Object(o.b)("h2",{id:"log-optiontf-a"},"Log ",Object(o.b)("inlineCode",{parentName:"h2"},"OptionT[F, A]")),Object(o.b)("h2",{id:"log-feithera-b"},"Log ",Object(o.b)("inlineCode",{parentName:"h2"},"F[Either[A, B]]")),Object(o.b)("h2",{id:"log-eithertf-a-b"},"Log ",Object(o.b)("inlineCode",{parentName:"h2"},"EitherT[F, A, B]")))}s.isMDXComponent=!0},71:function(e,n,t){"use strict";t.d(n,"a",(function(){return f})),t.d(n,"b",(function(){return m}));var r=t(0),a=t.n(r);function o(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function c(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){o(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var p=a.a.createContext({}),s=function(e){var n=a.a.useContext(p),t=n;return e&&(t="function"==typeof e?e(n):c(c({},n),e)),t},f=function(e){var n=s(e.components);return a.a.createElement(p.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return a.a.createElement(a.a.Fragment,{},n)}},g=a.a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,o=e.originalType,i=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),f=s(t),g=r,m=f["".concat(i,".").concat(g)]||f[g]||d[g]||o;return t?a.a.createElement(m,c(c({ref:n},p),{},{components:t})):a.a.createElement(m,c({ref:n},p))}));function m(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var o=t.length,i=new Array(o);i[0]=g;var c={};for(var l in n)hasOwnProperty.call(n,l)&&(c[l]=n[l]);c.originalType=e,c.mdxType="string"==typeof e?e:r,i[1]=c;for(var p=2;p<o;p++)i[p]=t[p];return a.a.createElement.apply(null,i)}return a.a.createElement.apply(null,t)}g.displayName="MDXCreateElement"}}]);