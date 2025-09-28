import clsx from 'clsx';
import React, {ReactNode} from 'react';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: <>Logger for <code>F[_]</code></>,
    Png: require('@site/static/img/io-2-200x200.png').default,
    description: (
      <>
        Logger-F is a tool for logging tagless final with an effect library.
      </>
    ),
  },
  {
    title: <>Abstraction + Log</>,
    Png: require('@site/static/img/f_-200x200.png').default,
    description: (
      <>
        So it lets you keep both abstraction and log with just a little extra code.
      </>
    ),
  },
  {
    title: <>Easy to log <code>Either</code> and <code>EitherT</code></>,
    Png: require('@site/static/img/exclusive-disjunction-200x200.png').default,
    description: (
      <>
        It has easy ways to log <code>Either</code> and <code>EitherT</code>.
      </>
    ),
  },
];

function FeatureSvg({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

function FeaturePng({Png, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <img src={Png} className={styles.featurePng} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            props.hasOwnProperty('Svg') ?
              <FeatureSvg key={idx} {...props} /> :
              <FeaturePng key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
