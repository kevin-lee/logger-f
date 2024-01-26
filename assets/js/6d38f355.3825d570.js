"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[309],{3384:(e,n,o)=>{o.r(n),o.d(n,{assets:()=>l,contentTitle:()=>r,default:()=>g,frontMatter:()=>s,metadata:()=>a,toc:()=>c});var i=o(5893),t=o(1151);const s={id:"log",title:"Log - Monix"},r=void 0,a={id:"docs/monix/log",title:"Log - Monix",description:"Log - Monix (WIP)",source:"@site/versioned_docs/version-v1/docs/monix/log.md",sourceDirName:"docs/monix",slug:"/docs/monix/log",permalink:"/docs/v1/docs/monix/log",draft:!1,unlisted:!1,tags:[],version:"v1",frontMatter:{id:"log",title:"Log - Monix"},sidebar:"docs",previous:{title:"Get LoggerF",permalink:"/docs/v1/docs/monix/getting-started"},next:{title:"Get LoggerF",permalink:"/docs/v1/docs/scalaz-effect/getting-started"}},l={},c=[{value:"Log - Monix (WIP)",id:"log---monix-wip",level:2},{value:"Log <code>F[A]</code>",id:"log-fa",level:2},{value:"Example",id:"example",level:3},{value:"Log <code>F[Option[A]]</code>",id:"log-foptiona",level:2},{value:"Log <code>OptionT[F, A]</code>",id:"log-optiontf-a",level:2},{value:"Log <code>F[Either[A, B]]</code>",id:"log-feithera-b",level:2},{value:"Log <code>EitherT[F, A, B]</code>",id:"log-eithertf-a-b",level:2}];function d(e){const n={a:"a",code:"code",h2:"h2",h3:"h3",p:"p",pre:"pre",...(0,t.a)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.h2,{id:"log---monix-wip",children:"Log - Monix (WIP)"}),"\n",(0,i.jsxs)(n.p,{children:[(0,i.jsx)(n.code,{children:"Log"})," is a typeclass to log ",(0,i.jsx)(n.code,{children:"F[A]"}),", ",(0,i.jsx)(n.code,{children:"F[Option[A]]"}),", ",(0,i.jsx)(n.code,{children:"F[Either[A, B]]"}),", ",(0,i.jsx)(n.code,{children:"OptionT[F, A]"})," and ",(0,i.jsx)(n.code,{children:"EitherT[F, A, B]"}),"."]}),"\n",(0,i.jsxs)(n.p,{children:["It requires ",(0,i.jsx)(n.code,{children:"Fx"})," from ",(0,i.jsx)(n.a,{href:"https://kevin-lee.github.io/effectie",children:"Effectie"})," and ",(0,i.jsx)(n.code,{children:"Monad"})," from ",(0,i.jsx)(n.a,{href:"https://typelevel.org/cats",children:"Cats"}),"."]}),"\n",(0,i.jsxs)(n.h2,{id:"log-fa",children:["Log ",(0,i.jsx)(n.code,{children:"F[A]"})]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-scala",children:"Log[F].log(F[A])(A => String)\n"})}),"\n",(0,i.jsx)(n.h3,{id:"example",children:"Example"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-scala",children:'trait Named[A] {\n  def name(a: A): String\n}\n\nobject Named {\n  def apply[A: Named]: Named[A] = implicitly[Named[A]]\n}\n\nfinal case class GivenName(givenName: String) extends AnyVal\nfinal case class Surname(surname: String) extends AnyVal\n\nfinal case class Person(givenName: GivenName, surname: Surname)\nobject Person {\n  implicit val namedPerson: Named[Person] =\n    person => s"${person.givenName.givenName} ${person.surname.surname}"\n}\n\nimport cats._\nimport cats.syntax.all._\nimport cats.effect.ExitCode\n\nimport effectie.monix.{ConsoleEffect, Fx}\nimport effectie.monix.Effectful._\n\nimport loggerf.logger._\nimport loggerf.monix._\nimport loggerf.syntax._\n\nimport monix.eval.Task\nimport monix.eval.TaskApp\n\n\ntrait Greeting[F[_]] {\n  def greet[A: Named](a: A): F[String]\n}\n\nobject Greeting {\n  def apply[F[_]: Greeting]: Greeting[F] = implicitly[Greeting[F]]\n\n  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =\n    new Greeting[F] {\n      def greet[A: Named](a: A): F[String] =\n        for {\n          name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))\n          greeting <- pureOf(s"Hello $name")\n        } yield greeting\n    }\n\n}\n\nobject TaskMainApp extends TaskApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  def run(args: List[String]): Task[ExitCode] =\n    for {\n      greetingMessage <- Greeting[Task].greet(\n        Person(GivenName("Kevin"), Surname("Lee"))\n      )\n      _ <- ConsoleEffect[Task].putStrLn(greetingMessage)\n    } yield ExitCode.Success\n}\n'})}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{children:"19:57:04.076 [scala-execution-context-global-21] INFO MyApp - The name is Kevin Lee\nHello Kevin Lee\n"})}),"\n",(0,i.jsxs)(n.h2,{id:"log-foptiona",children:["Log ",(0,i.jsx)(n.code,{children:"F[Option[A]]"})]}),"\n",(0,i.jsxs)(n.h2,{id:"log-optiontf-a",children:["Log ",(0,i.jsx)(n.code,{children:"OptionT[F, A]"})]}),"\n",(0,i.jsxs)(n.h2,{id:"log-feithera-b",children:["Log ",(0,i.jsx)(n.code,{children:"F[Either[A, B]]"})]}),"\n",(0,i.jsxs)(n.h2,{id:"log-eithertf-a-b",children:["Log ",(0,i.jsx)(n.code,{children:"EitherT[F, A, B]"})]})]})}function g(e={}){const{wrapper:n}={...(0,t.a)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},1151:(e,n,o)=>{o.d(n,{Z:()=>a,a:()=>r});var i=o(7294);const t={},s=i.createContext(t);function r(e){const n=i.useContext(s);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:r(e.components),i.createElement(s.Provider,{value:n},e.children)}}}]);