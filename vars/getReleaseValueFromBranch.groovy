#!/usr/bin/env groovy

def call(Map args = [:]) {
  def result = args.branch.split('/')
  return result[1]
}