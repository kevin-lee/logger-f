import {themes as prismThemes} from 'prism-react-renderer';

const algoliaConfig = require('./algolia.config.json');
const googleAnalyticsConfig = require('./google-analytics.config.json');

const lightCodeTheme = prismThemes.nightOwlLight;
const darkCodeTheme = prismThemes.nightOwl;

const isEmptyObject = (obj) => Object.keys(obj).length === 0;

const isSearchable = !isEmptyObject(algoliaConfig);
const hasGoogleAnalytics = !isEmptyObject(googleAnalyticsConfig);
const gtag = hasGoogleAnalytics ? { 'gtag': googleAnalyticsConfig } : null;

const websiteConfig = {
  title: 'logger-f',
  tagline: 'Logger for <code class="kev-title-code">F[_]</code>',
  url: 'https://logger-f.kevinly.dev',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'throw',
  favicon: 'img/favicon.png',
  organizationName: 'Kevin-Lee', // Usually your GitHub org/user name.
  projectName: 'logger-f', // Usually your repo name.
  themeConfig: {
    image: 'img/logger-f-social-card.jpg',
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
      additionalLanguages: [
        'java',
        'scala',
      ],
    },
    navbar: {
      title: 'logger-f',
      logo: {
        alt: 'logger-f Logo',
        src: 'img/logger-f-32x32.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          dropdownActiveClassDisabled: true,
          dropdownItemsAfter: [
            {
              to: '/versions',
              label: 'All versions',
            },
          ],
        },
        {
          href: 'https://github.com/Kevin-Lee/logger-f',
          className: 'header-github-link',
          'aria-label': 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: 'docs/',
            },
            {
              label: 'For Cats',
              to: 'docs/cats',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/Kevin-Lee/logger-f',
            },
            {
              label: 'Blog',
              href: 'https://blog.kevinlee.io',
            },
            {
              label: 'Homepage',
              href: 'https://kevinlee.io',
            },
          ],
        },
      ],
      copyright: `Copyright © 2020 logger-f is designed and developed by <a href="https://github.com/Kevin-Lee" target="_blank">Kevin Lee</a>.<br>The website built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          path: '../generated-docs/docs/',
          sidebarPath: require.resolve('./sidebars.js'),
          "lastVersion": "current",
          "versions": {
            "v1": {
              "label": "v1",
              "path": "v1",
            },
            "current": {
              "label": "2.0.0-beta24",
            },
          }
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        ...gtag,
      },
    ],
  ],
  plugins: [
    require.resolve('docusaurus-lunr-search'),
  ],
};

if (isSearchable) {
  websiteConfig['themeConfig']['algolia'] = algoliaConfig;
}

module.exports = websiteConfig;
