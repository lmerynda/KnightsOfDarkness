# Project info

## Useful configs

'[org.hibernate.orm.jdbc.bind]': TRACE
shows parameter binding

'[org.hibernate.type.descriptor.sql]': TRACE
shows queries

general purpose sql debug

  jpa:
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        use_sql_comments: true
        type: trace

