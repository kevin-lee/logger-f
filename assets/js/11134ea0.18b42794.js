"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[938],{8433:(e,n,o)=>{o.r(n),o.d(n,{assets:()=>s,contentTitle:()=>l,default:()=>g,frontMatter:()=>t,metadata:()=>c,toc:()=>r});var a=o(5893),i=o(1151);const t={id:"log",title:"Log - Scalaz"},l=void 0,c={id:"docs/scalaz-effect/log",title:"Log - Scalaz",description:"Log - Scalaz (WIP)",source:"@site/versioned_docs/version-v1/docs/scalaz-effect/log.md",sourceDirName:"docs/scalaz-effect",slug:"/docs/scalaz-effect/log",permalink:"/docs/v1/docs/scalaz-effect/log",draft:!1,unlisted:!1,tags:[],version:"v1",frontMatter:{id:"log",title:"Log - Scalaz"},sidebar:"docs",previous:{title:"Get LoggerF",permalink:"/docs/v1/docs/scalaz-effect/getting-started"}},s={},r=[{value:"Log - Scalaz (WIP)",id:"log---scalaz-wip",level:2},{value:"Log <code>F[A]</code>",id:"log-fa",level:2},{value:"Example",id:"example",level:3},{value:"Log <code>F[Option[A]]</code>",id:"log-foptiona",level:2},{value:"Log <code>OptionT[F, A]</code>",id:"log-optiontf-a",level:2},{value:"Log <code>F[A \\/ B]</code>",id:"log-fa--b",level:2},{value:"Log <code>EitherT[F, A, B]</code>",id:"log-eithertf-a-b",level:2}];function d(e){const n={a:"a",code:"code",h2:"h2",h3:"h3",p:"p",pre:"pre",...(0,i.a)(),...e.components};return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsx)(n.h2,{id:"log---scalaz-wip",children:"Log - Scalaz (WIP)"}),"\n",(0,a.jsxs)(n.p,{children:[(0,a.jsx)(n.code,{children:"Log"})," is a typeclass to log ",(0,a.jsx)(n.code,{children:"F[A]"}),", ",(0,a.jsx)(n.code,{children:"F[Option[A]]"}),", ",(0,a.jsx)(n.code,{children:"F[A \\/ B]"}),", ",(0,a.jsx)(n.code,{children:"OptionT[F, A]"})," and ",(0,a.jsx)(n.code,{children:"EitherT[F, A, B]"}),"."]}),"\n",(0,a.jsxs)(n.p,{children:["It requires ",(0,a.jsx)(n.code,{children:"Fx"})," from ",(0,a.jsx)(n.a,{href:"https://kevin-lee.github.io/effectie",children:"Effectie"})," and ",(0,a.jsx)(n.code,{children:"Monad"})," from ",(0,a.jsx)(n.a,{href:"https://github.com/scalaz/scalaz",children:"Scalaz"}),"."]}),"\n",(0,a.jsxs)(n.h2,{id:"log-fa",children:["Log ",(0,a.jsx)(n.code,{children:"F[A]"})]}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:"Log[F].log(F[A])(A => String)\n"})}),"\n",(0,a.jsx)(n.h3,{id:"example",children:"Example"}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{className:"language-scala",children:'trait Named[A] {\n  def name(a: A): String\n}\n\nobject Named {\n  def apply[A: Named]: Named[A] = implicitly[Named[A]]\n}\n\nfinal case class GivenName(givenName: String) extends AnyVal\nfinal case class Surname(surname: String) extends AnyVal\n\nfinal case class Person(givenName: GivenName, surname: Surname)\nobject Person {\n  implicit val namedPerson: Named[Person] =\n    person => s"${person.givenName.givenName} ${person.surname.surname}"\n}\n\nimport scalaz._\nimport Scalaz._\nimport scalaz.effect._\n\nimport effectie.scalaz.Fx\nimport effectie.scalaz.ConsoleEffect\nimport effectie.scalaz.Effectful._\n\nimport loggerf.logger._\nimport loggerf.scalaz._\nimport loggerf.syntax._\n\ntrait Greeting[F[_]] {\n  def greet[A: Named](a: A): F[String]\n}\n\nobject Greeting {\n  def apply[F[_] : Greeting]: Greeting[F] = implicitly[Greeting[F]]\n\n  implicit def hello[F[_]: Fx: Monad: Log]: Greeting[F] =\n    new Greeting[F] {\n      def greet[A: Named](a: A): F[String] = for {\n        name <- log(effectOf(Named[A].name(a)))(x => info(s"The name is $x"))\n        greeting <- pureOf(s"Hello $name")\n      } yield greeting\n    }\n\n}\n\nobject MyApp {\n\n  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("MyApp")\n\n  def run(args: List[String]): IO[Unit] = for {\n    greetingMessage <- Greeting[IO].greet(Person(GivenName("Kevin"), Surname("Lee")))\n    _ <- ConsoleEffect[IO].putStrLn(greetingMessage)\n  } yield ()\n\n  def main(args: Array[String]): Unit =\n    run(args.toList).unsafePerformIO()\n}\n'})}),"\n",(0,a.jsx)(n.pre,{children:(0,a.jsx)(n.code,{children:"21:02:15.323 [ioapp-compute-0] INFO MyApp - The name is Kevin Lee\nHello Kevin Lee\n"})}),"\n",(0,a.jsxs)(n.h2,{id:"log-foptiona",children:["Log ",(0,a.jsx)(n.code,{children:"F[Option[A]]"})]}),"\n",(0,a.jsxs)(n.h2,{id:"log-optiontf-a",children:["Log ",(0,a.jsx)(n.code,{children:"OptionT[F, A]"})]}),"\n",(0,a.jsxs)(n.h2,{id:"log-fa--b",children:["Log ",(0,a.jsx)(n.code,{children:"F[A \\/ B]"})]}),"\n",(0,a.jsxs)(n.h2,{id:"log-eithertf-a-b",children:["Log ",(0,a.jsx)(n.code,{children:"EitherT[F, A, B]"})]})]})}function g(e={}){const{wrapper:n}={...(0,i.a)(),...e.components};return n?(0,a.jsx)(n,{...e,children:(0,a.jsx)(d,{...e})}):d(e)}},1151:(e,n,o)=>{o.d(n,{Z:()=>c,a:()=>l});var a=o(7294);const i={},t=a.createContext(i);function l(e){const n=a.useContext(t);return a.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function c(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:l(e.components),a.createElement(t.Provider,{value:n},e.children)}}}]);