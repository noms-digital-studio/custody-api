{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: APPLICATION_INSIGHTS_IKEY
    valueFrom:
      secretKeyRef:
        key: APPLICATION_INSIGHTS_IKEY
        name: {{ template "app.name" . }}
  - name: SPRING_PROFILES_ACTIVE
    valueFrom:
      secretKeyRef:
        key: SPRING_PROFILES_ACTIVE
        name: {{ template "app.name" . }}
  - name: SPRING_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_URL
        name: {{ template "app.name" . }}
  - name: SPRING_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_USERNAME
        name: {{ template "app.name" . }}
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_PASSWORD
        name: {{ template "app.name" . }}
  - name: JWT_PUBLIC_KEY
    valueFrom:
      secretKeyRef:
        key: JWT_PUBLIC_KEY
        name: {{ template "app.name" . }}
{{- end }}
