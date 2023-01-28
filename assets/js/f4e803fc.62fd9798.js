"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[587],{3905:function(e,n,t){t.d(n,{Zo:function(){return c},kt:function(){return m}});var r=t(7294);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function i(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var p=r.createContext({}),g=function(e){var n=r.useContext(p),t=n;return e&&(t="function"==typeof e?e(n):i(i({},n),e)),t},c=function(e){var n=g(e.components);return r.createElement(p.Provider,{value:n},e.children)},s={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},f=r.forwardRef((function(e,n){var t=e.components,a=e.mdxType,o=e.originalType,p=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),f=g(t),m=a,d=f["".concat(p,".").concat(m)]||f[m]||s[m]||o;return t?r.createElement(d,i(i({ref:n},c),{},{components:t})):r.createElement(d,i({ref:n},c))}));function m(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var o=t.length,i=new Array(o);i[0]=f;var l={};for(var p in n)hasOwnProperty.call(n,p)&&(l[p]=n[p]);l.originalType=e,l.mdxType="string"==typeof e?e:a,i[1]=l;for(var g=2;g<o;g++)i[g]=t[g];return r.createElement.apply(null,i)}return r.createElement.apply(null,t)}f.displayName="MDXCreateElement"},903:function(e,n,t){t.r(n),t.d(n,{assets:function(){return c},contentTitle:function(){return p},default:function(){return m},frontMatter:function(){return l},metadata:function(){return g},toc:function(){return s}});var r=t(7462),a=t(3366),o=(t(7294),t(3905)),i=["components"],l={id:"log",title:"Log - Cats"},p=void 0,g={unversionedId:"cats-effect/log",id:"cats-effect/log",title:"Log - Cats",description:"Log - Cats (WIP)",source:"@site/../generated-docs/target/mdoc/cats-effect/log.md",sourceDirName:"cats-effect",slug:"/cats-effect/log",permalink:"/docs/cats-effect/log",tags:[],version:"current",frontMatter:{id:"log",title:"Log - Cats"},sidebar:"theSidebar",previous:{title:"Get LoggerF",permalink:"/docs/cats-effect/getting-started"},next:{title:"Get LoggerF",permalink:"/docs/monix/getting-started"}},c={},s=[{value:"Log - Cats (WIP)",id:"log---cats-wip",level:2},{value:"Log <code>F[A]</code>",id:"log-fa",level:2},{value:"Example",id:"example",level:3},{value:"Log <code>F[Option[A]]</code>",id:"log-foptiona",level:2},{value:"Example",id:"example-1",level:3},{value:"Log <code>F[Either[A, B]]</code>",id:"log-feithera-b",level:2},{value:"Example",id:"example-2",level:3},{value:"Log <code>OptionT[F, A]</code>",id:"log-optiontf-a",level:2},{value:"Log <code>EitherT[F, A, B]</code>",id:"log-eithertf-a-b",level:2}],f={toc:s};function m(e){var n=e.components,t=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},f,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("h2",{id:"log---cats-wip"},"Log - Cats (WIP)"),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Log")," is a typeclass to log ",(0,o.kt)("inlineCode",{parentName:"p"},"F[A]"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"F[Option[A]]"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"OptionT[F, A]")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"EitherT[F, A, B]"),"."),(0,o.kt)("p",null,"It requires ",(0,o.kt)("inlineCode",{parentName:"p"},"Fx")," from ",(0,o.kt)("a",{parentName:"p",href:"https://kevin-lee.github.io/effectie"},"Effectie")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"Monad")," from ",(0,o.kt)("a",{parentName:"p",href:"https://typelevel.org/cats"},"Cats"),"."),(0,o.kt)("h2",{id:"log-fa"},"Log ",(0,o.kt)("inlineCode",{parentName:"h2"},"F[A]")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"Log[F].log(F[A])(A => LogMessage)\n")),(0,o.kt)("p",null,"A given ",(0,o.kt)("inlineCode",{parentName:"p"},"F[A]"),", you can simply log ",(0,o.kt)("inlineCode",{parentName:"p"},"A")," with ",(0,o.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,o.kt)("h3",{id:"example"},"Example"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.cats._\nimport effectie.cats.Effectful._\n\nimport loggerf.cats._\nimport loggerf.logger._\nimport loggerf.syntax._\n\ndef hello[F[_]: Functor: Fx: Log](name: String): F[Unit] =\n  log(pureOf(s"Hello $name"))(debug).map(println(_))\n \nobject MyApp extends IOApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  def run(args: List[String]): IO[ExitCode] = for {\n    _ <- hello[IO]("World")\n    _ <- hello[IO]("Kevin")\n  } yield ExitCode.Success\n}\n\n')),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"23:34:25.021 [ioapp-compute-1] DEBUG MyApp - Hello World\nHello World\n23:34:25.022 [ioapp-compute-1] DEBUG MyApp - Hello Kevin\nHello Kevin\n")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'trait Named[A] {\n  def name(a: A): String\n}\n\nobject Named {\n  def apply[A: Named]: Named[A] = implicitly[Named[A]]\n}\n\nfinal case class GivenName(givenName: String) extends AnyVal\nfinal case class Surname(surname: String) extends AnyVal\n\nfinal case class Person(givenName: GivenName, surname: Surname)\nobject Person {\n  implicit val namedPerson: Named[Person] =\n    person => s"${person.givenName.givenName} ${person.surname.surname}"\n}\n\nimport cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.cats.Fx\nimport effectie.cats.ConsoleEffect\nimport effectie.cats.Effectful._\n\nimport loggerf.cats._\nimport loggerf.logger._\nimport loggerf.syntax._\n\ntrait Greeting[F[_]] {\n  def greet[A: Named](a: A): F[String]\n}\n\nobject Greeting {\n  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]\n\n  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =\n    new Greeting[F] {\n      def greet[A: Named](a: A): F[String] = for {\n        name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))\n        greeting <- pureOf(s"Hello $name")\n      } yield greeting\n    }\n\n}\n\nobject MyApp extends IOApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  def run(args: List[String]): IO[ExitCode] = for {\n    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))\n    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)\n  } yield ExitCode.Success\n}\n')),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"21:02:15.323 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee\nHello Kevin Lee\n")),(0,o.kt)("h2",{id:"log-foptiona"},"Log ",(0,o.kt)("inlineCode",{parentName:"h2"},"F[Option[A]]")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"Log[Option[F]].log(\n  F[Option[A]]\n)(\n  ifEmpty: => LogMessage with MaybeIgnorable,\n  toLeveledMessage: A => LogMessage with MaybeIgnorable\n)\n")),(0,o.kt)("p",null,"A given ",(0,o.kt)("inlineCode",{parentName:"p"},"F[Option[A]]"),", you can simply log ",(0,o.kt)("inlineCode",{parentName:"p"},"Some(A)")," or ",(0,o.kt)("inlineCode",{parentName:"p"},"None")," with ",(0,o.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,o.kt)("h3",{id:"example-1"},"Example"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.cats._\nimport effectie.cats.Effectful._\n\nimport loggerf.cats._\nimport loggerf.logger._\nimport loggerf.syntax._\n\ndef greeting[F[_]: Fx](name: String): F[String] =\n  pureOf(s"Hello $name")\n\ndef hello[F[_]: Monad: Fx: Log](maybeName: Option[String]): F[Unit] =\n  for {\n    name    <- log(pureOf(maybeName))(\n                 warn("No name given"),\n                 name => info(s"Name: $name")\n               )\n    message <- log(name.traverse(greeting[F]))(ignore, msg => info(s"Message: $msg"))\n    _       <- effectOf(message.foreach(msg => println(msg)))\n  } yield ()\n\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp- F[Option[A]]")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@653ee8ba\n\ndef run(): IO[Unit] = for {\n  _ <- hello[IO](none)\n  _ <- hello[IO]("Kevin".some)\n} yield ()\n\nrun().unsafeRunSync()\n// Hello Kevin\n')),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"20:09:43.117 [Thread-31] WARN MyApp- F[Option[A]] - No name given\n20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Name: Kevin\n20:09:43.133 [Thread-31] INFO MyApp- F[Option[A]] - Message: Hello Kevin\n")),(0,o.kt)("h2",{id:"log-feithera-b"},"Log ",(0,o.kt)("inlineCode",{parentName:"h2"},"F[Either[A, B]]")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"Log[Either[F]].log(\n  F[Either[A, B]]\n)(\n  leftToMessage: A => LeveledMessage with MaybeIgnorable,\n  rightToMessage: B => LeveledMessage with MaybeIgnorable\n)\n")),(0,o.kt)("p",null,"A given ",(0,o.kt)("inlineCode",{parentName:"p"},"F[Either[A, B]]"),", you can simply log ",(0,o.kt)("inlineCode",{parentName:"p"},"Left(A)")," or ",(0,o.kt)("inlineCode",{parentName:"p"},"Right(B)")," with ",(0,o.kt)("inlineCode",{parentName:"p"},"log"),"."),(0,o.kt)("h3",{id:"example-2"},"Example"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'import cats._\nimport cats.syntax.all._\nimport cats.effect._\n\nimport effectie.cats._\nimport effectie.cats.Effectful._\n\nimport loggerf.cats._\nimport loggerf.logger._\nimport loggerf.syntax._\n\ndef foo[F[_]: Fx](a: Int): F[Int] =\n  pureOf(a * 2)\n\ndef divide[F[_]: Fx: CanHandleError](a: Int, b: Int): F[Either[String, Int]] =\n  CanHandleError[F].handleNonFatal(effectOf((a / b).asRight[String])){ err =>\n    err.getMessage.asLeft[Int]\n  }\n\ndef calculate[F[_]: Monad: Fx: CanHandleError: Log](n: Int): F[Unit] =\n  for {\n    a      <- log(foo(n))(\n                n => info(s"n: ${n.toString}")\n              )\n    result <- log(divide(1000, a))(\n                err => error(s"Error: $err"),\n                r => info(s"Result: ${r.toString}")\n              )\n    _      <- effectOf(println(result.fold(err => s"Error: $err", r => s"1000 / ${a.toString} = ${r.toString}")))\n  } yield ()\n\n\nimplicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp - F[Either[A, B]]")\n// canLog: CanLog = loggerf.logger.Slf4JLogger@71f99800\n\ndef run(): IO[Unit] = for {\n  _ <- calculate[IO](5)\n  _ <- calculate[IO](0)\n} yield ()\n\nrun().unsafeRunSync()\n// 1000 / 10 = 100\n// Error: / by zero\n')),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"20:20:05.588 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 10\n20:20:05.593 [Thread-47] INFO MyApp - F[Either[A, B]] - Result: 100\n20:20:05.595 [Thread-47] INFO MyApp - F[Either[A, B]] - n: 0\n20:20:05.605 [Thread-47] ERROR MyApp - F[Either[A, B]] - Error: / by zero\n")),(0,o.kt)("h2",{id:"log-optiontf-a"},"Log ",(0,o.kt)("inlineCode",{parentName:"h2"},"OptionT[F, A]")),(0,o.kt)("h2",{id:"log-eithertf-a-b"},"Log ",(0,o.kt)("inlineCode",{parentName:"h2"},"EitherT[F, A, B]")))}m.isMDXComponent=!0}}]);