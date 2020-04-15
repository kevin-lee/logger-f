---
layout: docs
title: "Log - Cats"
---

# Log - Cats (WIP)

`Log` is a typeclass to log `F[A]`, `F[Option[A]]`, `F[Either[A, B]]`, `OptionT[F, A]` and `EitherT[F, A, B]`.

It requires `EffectConstructor` from [Effectie](https://kevin-lee.github.io/effectie) and `Monad` from [Cats](https://typelevel.org/cats).

# Log `F[A]`
```
Log[F].log(F[A])(A => String)
```

# Log `F[Option[A]]`

# Log `OptionT[F, A]`

# Log `F[Either[A, B]]`

# Log `EitherT[F, A, B]`
