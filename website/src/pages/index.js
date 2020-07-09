import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
  {
    title: <>Logger for <code>F[_]</code></>,
    imageUrl: 'img/io-2-200x200.png',
    description: (
      <>
        Logger-F is a tool for logging tagless final with an effect library.
      </>
    ),
  },
  {
    title: <>Abstraction + Log</>,
    imageUrl: 'img/f_-200x200.png',
    description: (
      <>
        So it lets you keep both abstraction and log with just a little extra code.
      </>
    ),
  },
  {
    title: <>Easy to log <code>Either</code> and <code>EitherT</code></>,
    imageUrl: 'img/exclusive-disjunction-200x200.png',
    description: (
      <>
        It has easy ways to log <code>Either</code> and <code>EitherT</code>.
      </>
    ),
  },
];

function Feature({imageUrl, title, description}) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx('col col--4', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Logger for F[_]">
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <img src={`${useBaseUrl('img/')}/poster.png`} alt="Project Logo" />
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle"><div dangerouslySetInnerHTML={{__html: siteConfig.tagline}} /></p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.getStarted,
              )}
              to={useBaseUrl('docs/')}>
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              <div className="row">
                {features.map((props, idx) => (
                  <Feature key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
